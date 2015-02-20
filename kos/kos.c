#include <kos.h>

/* inicialização dos semáforos para as threads cliente e servidora*/
sem_t **writers, **readers;

/* inicialização dos mutexes para os shards*/
pthread_mutex_t **read_write;

/* para os leitores/escritores */
int **nreaders, **blocked_readers, **blocked_writers, **writing;

/* para o getAllKeys */
extern int num_shards_g, num_server_threads_g;

/* vector de shards */
linkk **shard_array;										

void shard_array_init (int num_shards)
{
	int i, e;

	shard_array =  (linkk**) malloc (sizeof (linkk*)*num_shards);						/* inicializa o vector de shards */
	read_write = (pthread_mutex_t **) malloc (sizeof (pthread_mutex_t*)*num_shards);
	writers = (sem_t **) malloc (sizeof (sem_t*)*num_shards);
	readers = (sem_t **) malloc (sizeof (sem_t*)*num_shards);
	nreaders = (int **) malloc (sizeof (int*)*num_shards);
	blocked_writers = (int **) malloc (sizeof (int*)*num_shards);
	blocked_readers = (int **) malloc (sizeof (int*)*num_shards);
	writing = (int **) malloc (sizeof (int*)*num_shards);

	for (i = 0; i < num_shards; ++i)
	{
		shard_array[i] = (linkk*) malloc (sizeof(linkk)*HT_SIZE);						/* array de listas ligadas (hash table) */
		read_write[i] = (pthread_mutex_t*) malloc(sizeof(pthread_mutex_t)*HT_SIZE);
		writers[i] = (sem_t*) malloc(sizeof(sem_t)*HT_SIZE);
		readers[i] = (sem_t*) malloc(sizeof(sem_t)*HT_SIZE);
		nreaders[i] = (int*) malloc (sizeof(int)*HT_SIZE);
		blocked_readers[i] = (int*) malloc (sizeof(int)*HT_SIZE);
		blocked_writers[i] = (int*) malloc (sizeof(int)*HT_SIZE);
		writing[i] = (int*) malloc (sizeof (int)*HT_SIZE);

		for (e = 0; e < HT_SIZE; ++e)
		{
			shard_array[i][e] = NULL;
			pthread_mutex_init(&read_write[i][e], NULL);
			sem_init(&writers[i][e], 0, 0);
			sem_init(&readers[i][e], 0, num_server_threads_g);
			nreaders[i][e] = 0;
			blocked_readers[i][e] = 0;
			blocked_writers[i][e] = 0;
			writing[i][e] = FALSE;
		}
	}

	file_init (num_shards); 													/* carrega o conteúdo dos ficheiros para dentro dos shards*/
}


char *get (int clientId, int shardId, char* key)
{	
	linkk aux;
	char *prev_value, *temp_string;
	
	if (shardId >= num_shards_g || shardId < 0)		/* testa se os argumentos passados á função são válidos */
		return NULL;

	start_reading (shardId, hash(key));							
	aux = shard_array [shardId][hash(key)];	
	temp_string = list_get (aux, key);										/* procura pela chave na lista ligada presente na posição 'hash(key)' da hashtable */
	finish_reading (shardId, hash(key));

	if (temp_string != NULL)
	{																			/* cria um duplicado da string se esta tiver sido encontrada */
		prev_value = (char *) malloc (sizeof(char)*(strlen(temp_string)+1));
		strcpy (prev_value, temp_string);	
	}

	else
		prev_value = NULL;
		
	return prev_value;											/* e retorna o valor associado á key pedida */
}

char *put (int clientId, int shardId, char* key, char* value) 
{
	linkk aux;
	char *prev_value;

	if (shardId >= num_shards_g || shardId < 0)					/* testa se os argumentos passados á função são válidos */
		return NULL;
	
	prev_value = list_put (shardId, key, value);							/* insere o KV na lista */

	start_reading(shardId, hash(key));
	aux = get_node (shard_array[shardId][hash(key)], key);
	finish_reading(shardId, hash(key));

	write_pair (shardId, aux);											/* insere o KV no ficheiro respectivo */

	return prev_value;
}

char* list_put  (int shardId, char* key, char* value)
{	
	linkk aux;
	char *prev_value, *temp_string;

	start_reading(shardId, hash(key));
	aux = shard_array [shardId][hash(key)];									/* obtém o 1º nó da lista identificada pela key */
	temp_string = list_get (aux, key);										/* procura pela chave na lista ligada presente na posição 'hash(key)' da hashtable */
	finish_reading(shardId, hash(key));

	if (temp_string != NULL)
	{																			/* cria um duplicado da string se esta tiver sido encontrada */
		prev_value = (char *) malloc (sizeof(char)*(strlen(temp_string)+1));
		strcpy (prev_value, temp_string);	
	}

	else
		prev_value = NULL;
	
	shard_array [shardId][hash(key)] = list_insert (aux, key, value, shardId);
	finish_writing(shardId, hash(key));

	return prev_value;
}

char *delete(int clientId, int shardId, char* key)
{
    linkk aux;
	char *prev_value, *temp_string;
	
	if (shardId >= num_shards_g || shardId < 0)		/* testa se os argumentos passados á função são válidos */
		return NULL;

	start_reading(shardId, hash(key));
	aux = shard_array [shardId][hash(key)];
	temp_string = list_get (aux, key);
	finish_reading (shardId, hash(key));

	if (temp_string == NULL)											/* se a string não tiver sido encontrada */
		return NULL;
																					
	prev_value = (char *) malloc (sizeof(char)*(strlen(temp_string)+1)); 			/* caso um valor tenha sido encontrado */
	strcpy (prev_value, temp_string);												/* cria um duplicado do valor */
		
	start_reading (shardId, hash(key));
	aux = get_node (shard_array[shardId][hash(key)], key);
	finish_reading (shardId, hash(key));

	delete_pair (shardId, aux->offset);														/* remove o par nos ficheiros persistentes */
	
	aux = shard_array [shardId][hash(key)];

	shard_array [shardId][hash(key)] = list_remove(aux, key, shardId);						/* remove o par chave-valor do KOS */
	finish_writing(shardId, hash(key));

	return prev_value;
}


KV_t* getAllKeys(int clientId, int shardId, int* sizeKVarray)
{
	KV_t *array_pairs = NULL;
	linkk aux;
	int i;
	
	if (shardId >= num_shards_g || shardId < 0)		/* testa se os argumentos passados á função são válidos */
	{
		*sizeKVarray = -1;																	/* se um dos argumentos for inválido, o dim é igualado a -1 */
		return NULL;																/* e a função termina */	
	}
	
	*sizeKVarray = 0;

	for (i = 0; i < HT_SIZE; ++i)							/* insere todos os pares chave-valor num vector e mantém um registo do nº de pares encontrados*/
	{
		start_reading(shardId, i);
		aux = shard_array [shardId][i];
		array_pairs = list_get_all_pairs (aux, array_pairs, sizeKVarray);			
		finish_reading(shardId, i);
	}

	return array_pairs;	
}

void set_offset (char* key, int shardId, int offset)
{
	linkk item = get_node (shard_array[shardId][hash(key)], key);
	item->offset = offset;
}

void start_reading (int shardId, int list_pos)
{
	pthread_mutex_lock (&read_write[shardId][list_pos]);

	if (writing[shardId][list_pos] || blocked_writers[shardId][list_pos] > 0)
	{
		++blocked_readers[shardId][list_pos];
		pthread_mutex_unlock (&read_write[shardId][list_pos]);
		sem_wait (&readers[shardId][list_pos]);
		pthread_mutex_lock (&read_write[shardId][list_pos]);

		if (blocked_readers[shardId][list_pos] > 0)
		{
			++nreaders[shardId][list_pos];
			--blocked_readers[shardId][list_pos];
			sem_post (&readers[shardId][list_pos]);
		}
	} 

	else 
		++nreaders[shardId][list_pos];

	pthread_mutex_unlock (&read_write[shardId][list_pos]);
}

void finish_reading (int shardId, int list_pos)
{
	pthread_mutex_lock (&read_write[shardId][list_pos]);

	--nreaders[shardId][list_pos];

	if (!nreaders[shardId][list_pos] && blocked_writers[shardId][list_pos] > 0)
	{
		sem_post (&writers[shardId][list_pos]);
		writing[shardId][list_pos] = TRUE;
		--blocked_writers[shardId][list_pos];
	}

	pthread_mutex_unlock (&read_write[shardId][list_pos]);
}

void start_writing (int shardId, int list_pos)
{
	pthread_mutex_lock (&read_write[shardId][list_pos]);

	if (writing[shardId][list_pos] || nreaders[shardId][list_pos] > 0 || blocked_readers[shardId][list_pos] > 0)
	{
		++blocked_writers[shardId][list_pos];
		pthread_mutex_unlock (&read_write[shardId][list_pos]);
		sem_wait (&writers[shardId][list_pos]);
		pthread_mutex_lock (&read_write[shardId][list_pos]);
	}
	writing[shardId][list_pos] = TRUE;
	pthread_mutex_unlock (&read_write[shardId][list_pos]);
}

void finish_writing (int shardId, int list_pos)
{
	pthread_mutex_lock (&read_write[shardId][list_pos]);
	writing[shardId][list_pos] = FALSE;

	if (blocked_readers[shardId][list_pos] > 0)
	{
		sem_post (&readers[shardId][list_pos]);
		++nreaders[shardId][list_pos];
		--blocked_readers[shardId][list_pos];
	}

	else if (blocked_writers[shardId][list_pos] > 0)
	{
		sem_post(&writers[shardId][list_pos]);
		writing[shardId][list_pos] = TRUE;
		--blocked_writers[shardId][list_pos];
	}
	pthread_mutex_unlock (&read_write[shardId][list_pos]);
}
