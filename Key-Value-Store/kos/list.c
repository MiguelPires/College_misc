#include <list.h>

linkk list_insert (linkk item, char *key, char *value, int shardId)
{
	if (item == NULL)
	{	
		start_writing(shardId, hash(key));
		item = (linkk) malloc (sizeof(struct node));										/* cria um novo nó na lista */
		item->pair = (KV_t*) malloc (sizeof(struct KV_t));								/* cria uma estrutura key-value para estar no nó */

		strcpy (item->pair->key, key);
		strcpy (item->pair->value, value);

		item->offset = -1;
		item->next = NULL;

	}

	else if (!strcmp (item->pair->key, key))					/*se já existir um key com o par então o valor antigo é esmagado pelo novo*/
	{
		start_writing(shardId, hash(key));
		strcpy (item->pair->value, value);
	}


	else
		item->next = list_insert (item->next, key, value, shardId);

	return item;
}

linkk list_remove(linkk item, char *key, int shardId)
{
	if (item == NULL)
		return NULL;

	else if (!strcmp(item->pair->key, key))
	{
		start_writing(shardId, hash(key));
		linkk aux = item->next;
		free (item->pair);
		free (item);
		return aux;
	}

	else 
	{
		item->next = list_remove (item->next, key, shardId);
		return item;
	}
}

char* list_get (linkk item, char *key)
{

	KV_t* pair = list_get_pair (item, key);

	if (pair == NULL)
		return NULL;

	return pair->value;

}

KV_t* list_get_pair (linkk item, char *key)
{
	linkk aux = get_node (item, key);

	if (aux == NULL)
		return NULL;
	
	else
		return aux->pair;
}

linkk get_node (linkk item, char* key)
{
	if (item == NULL)
		return NULL;

	else
	{
		if (!strcmp (item->pair->key, key))
			return item;

		else
			return get_node (item->next, key);
	}
}

KV_t* list_get_all_pairs (linkk aux, KV_t *array_pairs, int *arraySize)
{

	if (aux == NULL)
		return array_pairs;

	for (; aux != NULL; aux = aux->next)
	{
		++(*arraySize);

		if (array_pairs == NULL)
			array_pairs = (KV_t*) malloc (sizeof(KV_t));				

		else
			array_pairs = (KV_t*) realloc (array_pairs, sizeof(KV_t)*(*arraySize));								

		strcpy (array_pairs[(*arraySize)-1].value, aux->pair->value);
		strcpy (array_pairs[(*arraySize)-1].key, aux->pair->key);		
	}

	return array_pairs;	
}