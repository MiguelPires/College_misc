#ifndef KOS
#define KOS 1

#define TRUE 1
#define FALSE 0

#include <kos_client.h>
/* linkk - implementação da lista simplesmente ligada */
typedef struct node {
   KV_t *pair;
   struct node *next;
   int offset;
} *linkk;

#include <semaphore.h>
#include <pthread.h>
#include <hash.h>
#include <kos_file.h>
#include <list.h>

void shard_array_init (int num_shards);
char* get (int clientId, int shardId, char* key);
char* put (int clientId, int shardId, char* key, char* value);
char* list_put  (int shardId, char* key, char* value);
char *delete (int clientId, int shardId, char* key);
KV_t* getAllKeys(int clientId, int shardId, int* sizeKVarray);
void set_offset (char* key, int shardId, int offset);
void start_reading(int shardId, int list_pos);
void finish_reading(int shardId, int list_pos);
void start_writing(int shardId, int list_pos);
void finish_writing(int shardId, int list_pos);
#endif
