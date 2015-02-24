#include "bitmap.h"

void set_bit (int line, int column)
{
	int byte, bit;
	byte = column / CHAR_BIT;
	bit = column % CHAR_BIT;

	bitmap[line][byte] |= 0x80 >> bit;
}

void clear_bit (int line, int column)
{
	int byte, bit;
	byte = column / CHAR_BIT;
	bit = column % CHAR_BIT;

	bitmap[line][byte] &= ~(0x80 >> bit);
}

int get_bit (int line, int column)
{
	int byte, bit;
	byte = column / CHAR_BIT;
	bit = column % CHAR_BIT;

	return (bitmap[line][byte] & 0x80 >> bit) >> (7-bit);
}

int initBitmap(int lines, int columns)
{
	int i, e;

	bitmap = (char**) malloc (sizeof(char)*lines);

	for (i = 0; i < lines; ++i)
		bitmap[i] = (char*) calloc (columns/CHAR_BIT+1, sizeof(char));

}
