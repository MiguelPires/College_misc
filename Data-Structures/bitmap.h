#include <stdio.h>
#include <stdlib.h>
#include <limits.h>

char **bitmap;

void set_bit (int line, int column);
void clear_bit (int line, int column);
int get_bit (int line, int column);
int initBitmap(int lines, int columns);
