#ifndef BUFFER
#define BUFFER 1

#include <stdlib.h>
#include <pthread.h>
#include <string.h>
#include <stdio.h>
#include <semaphore.h>
#include <kos_client.h>
#include <kos.h>

/* a estrutura request que contém a informação relativa aos pedidos e ás respostas*/
typedef struct req					
{					
	char *op, *key, *value, *response;				/* "op" é o tipo de operação que a tarefa servidora tem de fazer (get, put, remove) */
	int shardId, *dim;
	KV_t *KV_array;	
	sem_t sem;						
} request;

/* Funções */

void buffer_init (int buf_size);
request* store_request (int clientId, int shardId, int *dim, char* key, char* value, char* op);
void store_field (char** struct_field, char *value);
#endif
