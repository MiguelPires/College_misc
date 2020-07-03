#include <kos_client.h>
#include <buffer.h>
#include <stdlib.h>
#include <pthread.h>
#include <string.h>
#include <semaphore.h>
#include <kos_server.h>

extern int prod_pointer;
int buf_size_g, num_shards_g, num_server_threads_g;
extern sem_t semaphore_prod;

int kos_init (int num_server_threads, int buf_size, int num_shards) 
{
	buf_size_g = buf_size;															/* guarda as variáveis de inicialização para fazer verificar os argumentos */
	num_shards_g = num_shards;
	num_server_threads_g = num_server_threads;
	
	buffer_init (buf_size);															/* inicializa o buffer */
	shard_array_init (num_shards);													/* inicializa o vector de shards*/

	if (server_thread_init (num_server_threads) == -1)								/* inicializa as threads servidoras */
		return -1;

	return 0;
}

char* kos_get (int clientid, int shardId, char* key) 
{
	char *response;
	request* aux;

	sem_wait (&semaphore_prod);
	aux = store_request (clientid, shardId, NULL, key, NULL, "get");
	sem_wait(&aux->sem);
	
	response = aux->response;													/* vai buscar a resposta do get ao buffer */

	free(aux);
	return response;
}

char* kos_put (int clientid, int shardId, char* key, char* value) 
{
	char *response;
	request* aux;

	sem_wait (&semaphore_prod);
	aux = store_request (clientid, shardId, NULL, key, value, "put");	
	sem_wait(&aux->sem);	
	
	response = aux->response;									/* vai buscar a resposta do put ao buffer */

	free(aux);
	return response;
}

char* kos_remove (int clientid, int shardId, char* key) 
{
	char *response;
	request* aux;

	sem_wait (&semaphore_prod);
	aux = store_request (clientid, shardId, NULL, key, NULL, "remove");			/* guarda os argumentos relativos ao pedido de remoção*/
	sem_wait(&aux->sem);

	response = aux->response;	

	free(aux);
	return response;
}
 
KV_t* kos_getAllKeys (int clientid, int shardId, int* dim) 
{
	KV_t *pairs_array;
	request* aux;

	sem_wait (&semaphore_prod);
	aux = store_request (clientid, shardId, dim, NULL, NULL, "getAllKeys");		/* guarda os argumentos relativos ao pedido*/
	sem_wait(&aux->sem);
	
	pairs_array = aux->KV_array;								/* vai buscar o vector de pares ao buffer */
	
	free(aux);
	return pairs_array; 
}


