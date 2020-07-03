#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <kos.h>

/* lst_insert - insert a new item with value 'value' in list 'list' */
linkk list_insert (linkk item, char *key, char *value, int shardId);

/* lst_remove - remove first item of value 'value' from list 'list' */
linkk list_remove (linkk item, char *key, int shardId);

char* list_get (linkk item, char *key);
 
KV_t* list_get_pair (linkk item, char *key);

linkk get_node (linkk item, char* key);

/* list_insert_pairs - insert pairs form linkk in array_pairs */
KV_t* list_get_all_pairs (linkk aux, KV_t *array_pairs, int *arraySize);
