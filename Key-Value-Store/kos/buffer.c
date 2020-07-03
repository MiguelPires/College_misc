#include <buffer.h>

int prod_pointer = 0;	/* Indica qual a posição do buffer onde a thread cliente pode escrever o pedido */
int cons_pointer = 0; 	/* Indica qual a posição do buffer onde a thread servidora lê o pedido */

extern int buf_size_g;

sem_t semaphore_prod;	/* Semáforo das threads produtoras */
sem_t semaphore_cons;	/* Semáforo das threads consumidoras */

pthread_mutex_t mutex_write;

/* buffer onde vão ser postos os pedidos e respostas */
request **buffer;

void buffer_init (int buf_size)
{
	int i;
	
	buffer = (request**) malloc (sizeof(request*)*buf_size);			/* o buffer contém uma série de ponteiros para a estrutura com a request */

	for (i = 0; i < buf_size; ++i)
		buffer[i] = NULL;

	sem_init(&semaphore_prod, 0, buf_size_g);
	sem_init(&semaphore_cons, 0, 0);
}

request* store_request (int clientId, int shardId, int *dim, char* key, char* value, char* op)
{
	request* aux = (request*) calloc (1, sizeof(struct req));
	aux->shardId = shardId;						/* guarda qual é o shard onde vai ser feita a operação */
	store_field (&aux->value, value);				/* guarda o value na estrutura */
	store_field (&aux->key, key);					/* guarda a key na estrutura */
	store_field (&aux->op, op);					/* guarda o tipo de operação a efectuar na estrutura */
	sem_init (&aux->sem, 0, 0);
	
	if (!strcmp (op, "getAllKeys"))
		aux->dim = dim;

	pthread_mutex_lock(&mutex_write);
	buffer[prod_pointer] = aux;
	prod_pointer = (prod_pointer+1) % buf_size_g;
	pthread_mutex_unlock(&mutex_write);

	sem_post (&semaphore_cons);
	return aux;	
}

/* função genérica que guarda um valor qualquer num campo de uma estrutura */

void store_field (char** struct_field, char *value)
{
	if (value != NULL)																	/* se existir um valor para inserir na estrutura */
	{
		if (*struct_field != NULL)														/* se já existir um valor no campo, realoca memória */
			*struct_field = (char *) realloc (*struct_field, sizeof (char)*(strlen(value)+1));
	
		else																			/* se não ainda existir um valor no campo, aloca a memória necessária */
			*struct_field  = (char *) malloc (sizeof (char)*(strlen(value)+1));

		strcpy (*struct_field, value);													/* copia o valor para a estrutura request */
	}	
	
	else															
		if (*struct_field != NULL)														/* se não existir um valor para inserir no campo e houver memória alocada, liberta a memória*/
		{
			free (*struct_field);
			*struct_field = NULL; 
		}																				/* caso contrário a não faz nada porque o campo já foi inicializado a NULL */
}
