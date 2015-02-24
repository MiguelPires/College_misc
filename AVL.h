#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

typedef char *Word;

typedef struct node{ 
    Word word; 
    int *lines, occurrences, *positions, depth;
    struct node *left, *right;

}*Link; 


Link insertNode (Link root, Word aux_word, int line, int position);
Link newNode (Word aux_word, int line, int position);
Link updateNode (Link root, int line, int position);
void traverse (Link root);
void visit (Link root);
void deleteTree (Link root);
Link deleteWord (Link root, Word word);
Link deleteLink (Link root);
void deleteNode (Link root);
Link maxLeft (Link root, Link temp);
Link lookUp (Link root, Word word);
Link getDepth (Link root);
Link updateDepth (Link root);
int balanceFactor(Link root);
Link testBranch (Link root);
Link balanceTree (Link root, int bfactor);
Link rotateLeft (Link root);
Link rotateRight (Link root);
Link doubleRotateRL (Link root);
Link doubleRotateLR (Link root);
