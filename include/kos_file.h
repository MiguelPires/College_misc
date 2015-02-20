#ifndef FILE_H
#define FILE_H 1

#include <unistd.h>
#include <kos.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <limits.h>

void file_init (int num_shards);
void write_pair (int shardId, linkk item);
void update_shard (int shardId, linkk item);
void write_new_shard (int shardId, linkk item);
void delete_pair (int shardId, int offset);
char* shard_name (int shardId);
void set_bit (int shardId, int index);
void clear_bit (int shardId, int index);
int get_bit (int shardId, int index);
#endif