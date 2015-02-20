#include <kos_file.h>

/* bitmap com as posições livres de cada shard*/
char **bitmap;

pthread_mutex_t *file_mutex;

void file_init (int num_shards)
{
	int i, e, num;
	char *name;
	FILE *file;
	KV_t pair;

	file_mutex = (pthread_mutex_t*) malloc (sizeof (pthread_mutex_t)*num_shards);
	bitmap = (char**) malloc (sizeof(char*)*num_shards);

	for (i = 0; i < num_shards; ++i)
	{
		name = shard_name (i);
		file = fopen (name, "rb");
		
		if (file == NULL)
		{
			bitmap[i] = (char*) malloc (sizeof(char));
			clear_bit (i, 0);
			continue;
		}
		
		fread (&num, sizeof(int), 1, file);
	
		bitmap[i] = (char*) malloc (sizeof(char)*(num/CHAR_BIT+1));

		for (e = 0; e < num; ++e)
		{
			fread (&pair, sizeof(KV_t), 1, file);
			list_put (i, pair.key, pair.value);
			set_offset (pair.key, i, e);
			set_bit (i, e);
		}

		if (fclose (file) != 0)	
			printf("**file_init** Erro a fechar o ficheiro.\n");
		free (name);
	}
}

void write_pair (int shardId, linkk item)
{
	char *name = shard_name (shardId);
	FILE *file = fopen (name, "rb");

	if (file != NULL)
	{
		if (fclose (file) != 0)
			printf("**write_shard** Erro a fechar o ficheiro.\n");

		update_shard (shardId, item);
	}

	else
		write_new_shard (shardId, item);

	free (name);
}

void write_new_shard (int shardId, linkk item)
{
	char *name = shard_name (shardId);
	pthread_mutex_lock (&file_mutex[shardId]);
	FILE *file = fopen (name, "wb");
	int num = 1;

	fwrite (&num, sizeof(int), 1, file);
	fwrite (item->pair, sizeof(KV_t), 1, file);

	item->offset = 0;
	set_bit (shardId, 0);

	if (fclose (file) != 0)
		printf("**write_new_shard** Erro a fechar o ficheiro.\n");

	pthread_mutex_unlock (&file_mutex[shardId]);
	free (name);
}

void update_shard (int shardId, linkk item)
{
	int i, num, offset = item->offset;
	char *name = shard_name (shardId);
	pthread_mutex_lock (&file_mutex[shardId]);
	FILE *file = fopen (name, "r+b");

	fread (&num, sizeof(int), 1, file);							


	if (offset == -1)													/* se o par não existe, testa se existem posições livres */
		for (i = 0; i < num; ++i)
			if (!get_bit(shardId, i))
			{
				offset = i;
				break;
			}

	if (offset == -1)													/* se não existirem escreve um novo par no fim do ficheiro */
	{
		++num;
		fseek (file, 0, SEEK_SET);
		fwrite (&num, sizeof(int), 1, file);							/* incrementa o contador de pares */	

		if ((num-1)/CHAR_BIT != num/CHAR_BIT)
			bitmap[shardId] = realloc (bitmap[shardId], sizeof(char)*(num/CHAR_BIT +1));

		set_bit (shardId, num-1);

		item->offset = num-1;
		fseek (file, 0, SEEK_END);										/* escreve o par no final do ficheiro */
		fwrite (item->pair, sizeof(KV_t), 1, file);
	}

	else																/* se o par já existir ou se houver um posição vaga */
	{
		fseek (file, sizeof(int)+sizeof(KV_t)*offset, SEEK_SET);
		fwrite (item->pair, sizeof(KV_t), 1, file);
		set_bit (shardId, offset);
	}

	if (fclose (file) != 0)
		printf("**update_shard** Erro a fechar o ficheiro.\n");
		
	pthread_mutex_unlock (&file_mutex[shardId]);
	free (name);
}

void delete_pair (int shardId, int offset)
{
	pthread_mutex_lock (&file_mutex[shardId]);
	clear_bit (shardId, offset);
	pthread_mutex_unlock (&file_mutex[shardId]);
}

char* shard_name (int shardId)
{
	char *shard, *filename;
	int num;

	num = snprintf (NULL, 0, "%d", shardId);
	shard = (char*) malloc (sizeof(char) * (num+1));
	sprintf (shard, "%d", shardId);

	filename = (char*) malloc (sizeof(char) * (num+2));
	strcpy (filename, "f");
	strcat (filename, shard);

	free (shard);
	return filename;
}

void set_bit (int shardId, int index)
{
	int byte, bit;
	byte = index / CHAR_BIT;
	bit = index % CHAR_BIT;

	bitmap[shardId][byte] |= 0x80 >> bit;
}

void clear_bit (int shardId, int index)
{
	int byte, bit;
	byte = index / CHAR_BIT;
	bit = index % CHAR_BIT;

	bitmap[shardId][byte] &= ~(0x80 >> bit);
}

int get_bit (int shardId, int index)
{
	int byte, bit;
	byte = index / CHAR_BIT;
	bit = index % CHAR_BIT;

	return (bitmap[shardId][byte] & 0x80 >> bit) >> (7-bit);
}