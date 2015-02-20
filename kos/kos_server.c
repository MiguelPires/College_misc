#include <kos_server.h>

extern int cons_pointer, buf_size_g;
extern sem_t semaphore_prod, semaphore_cons;
pthread_mutex_t mutex_read;
pthread_t *server_thread_array;
extern request **buffer;

int server_thread_init (int num_server_threads)
{
	int i;
 
 	server_thread_array = (pthread_t*) malloc (sizeof(pthread_t)*num_server_threads);

	for (i = 0; i < num_server_threads; i++)	
	{			
		if (pthread_create (&server_thread_array[i], NULL, read_request, (void *) &i) == -1)
			return -1;
	}

	return 0;
}

void *read_request (int *thread_id)
{	
	request *req;
	char *aux_string;

	while (1)  
	{
		sem_wait (&semaphore_cons);
				
		pthread_mutex_lock(&mutex_read);
		req = buffer[cons_pointer];
		cons_pointer = (cons_pointer+1) % buf_size_g;
		pthread_mutex_unlock (&mutex_read);	

		sem_post(&semaphore_prod);
		//delay();

		if (!strcmp (req->op, "get"))														
		{
			aux_string = get (*thread_id, req->shardId, req->key);									/* obtém o valor associado á key */
			store_field (&(req->response), aux_string);											/* guarda-o no campo de resposta da estrutura do buffer*/

			sem_post (&req->sem);
			free (aux_string);	
		}

		else if (!strcmp (req->op, "put"))
		{	

			aux_string = put (*thread_id, req->shardId, req->key, req->value);				/* associa um valor a uma chave no kos */
			store_field (&(req->response), aux_string);        									/* devolve o anterior valor da chave através do buffer */

			sem_post (&req->sem);
			free (aux_string);
		}

		else if (!strcmp (req->op, "remove"))
		{
			aux_string = delete (*thread_id, req->shardId, req->key);		
			store_field (&req->response, aux_string);												/* devolve o valor da chave através do buffer */

			sem_post (&req->sem);
			free (aux_string);
		}
		
		else 
		{
			KV_t *array_pairs;
			array_pairs = getAllKeys(*thread_id, req->shardId, req->dim);			
			req->KV_array = array_pairs;

			sem_post (&req->sem);
		}
	}
	return NULL;
}
