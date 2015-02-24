#include "node.h"	

static int wasBalanced = 0;											/* evita testar se a árvore está desiquilibrada desnecessáriamente */

Link insertNode (Link root, Word aux_word, int line, int position)
{
	if (root == NULL)
	{
		root = newNode (aux_word, line, position);									/* se não existir, é criado um node para a nova palavra */
		wasBalanced = 0;															/* se um node foi inserido então pode ter desiquilibrado a árvore */
	}

	else if (!strcmp(aux_word, root->word))										/* se existir, os seus campos são actualizados */
		root = updateNode(root, line, position);
	
	else if (strcmp(aux_word, root->word) > 0)									/* a árvore é percorrida até chegar á posição onde a palavra está ou deve ser inserida */
		root->right = insertNode (root->right, aux_word, line,  position);

	else
		root->left = insertNode (root->left, aux_word, line,  position);
	
	root = updateDepth(root);														/* actualiza a profundidade dos nodes */

	if (!wasBalanced)																/* se a árvore já foi equilibrada após a inserção então não precisa de ser outra vez*/
		return (root = testBranch (root));
		
	return root;

}


Link newNode (Word aux_word, int line, int position)
{
	Link newlink = (Link) malloc (sizeof(struct node));							/*	reserva memória para a estrutura, a palavra, o vector com linhas e o vector com posições */
	newlink->word = (Word) malloc (sizeof(char)*(strlen(aux_word)+1));
	strcpy(newlink->word, aux_word);

	newlink->lines = (int*) malloc (sizeof(int));								/* os vectores para as linhas e posições são inicializados com espaço para uma célula */
	newlink->positions = (int*) malloc (sizeof(int));
	
	newlink->lines[0] = line;													/* e é guardada a linha e posição da 1ª ocorrência nessa célula */
	newlink->positions[0] = position;
	newlink->depth = -1;								
	newlink->occurrences = 1;	

	newlink->left = NULL;																								
	newlink->right = NULL;

	return newlink;
}

Link updateNode (Link root, int line, int position)
{	
	root->occurrences++;																	/* se a palavra já estiver inserida na árvore então incrementa as ocorrências */
	root->positions = (int*) realloc (root->positions, sizeof(int)*(root->occurrences));	/* e realoca memória para uma nova linha e posição*/
	root->lines = (int*) realloc (root->lines, sizeof(int)*(root->occurrences));
	
	root->lines[root->occurrences-1] =  line;	
	root->positions[root->occurrences-1] = position;										/* guarda a linha e a posição onde ocorreu */

	return root;
}

void traverse (Link root)
{
	if (root == NULL)												
		return;		

	traverse(root->left);										/* faz uma travessia in order que imprime a árvore alfabeticamente */
	visit(root);
	traverse(root->right);
}

void visit (Link root)
{
	printf("%s %d\n", root->word, root->occurrences);							
}


Link deleteWord (Link root, Word word)
{
	if (root != NULL)
	{
		if (!strcmp(word, root->word))			/* percorre a árvore até encontrar um node com a palavra correspondente */
			root = deleteLink(root);
	
		else if (strcmp(word, root->word) > 0)
			root->right = deleteWord(root->right, word);

		else 
			root->left = deleteWord(root->left, word);
	}

	root = updateDepth(root);							/* actualiza a profundidade dos nodes e verifica se a árvore ficou desiquilibrada */
	
	return testBranch (root);
}


Link deleteLink (Link root)
{
	Link aux, temp;

	if (root->right != NULL && root->left != NULL)		/* caso o node a apagar tenha dois filhos */
	{
		if (root->left->right != NULL)
		{
			temp = maxLeft(root->left, NULL);			/* o pai do maior filho do ramo esquerdo */
			aux = temp->right;							/* o maior filho do ramo esquerdo */
			aux->right = root->right;					/* assume o filho direito da raiz */
			temp->right = aux->left;					/* o seu filho esquerdo passa a ser o maior do ramo esquerdo */
			aux->left = root->left;	
		}
		else
		{
			aux = root->left;							/* o maior filho do ramo esquerdo é o filho esquerdo da raiz */
			aux->right = root->right;					/* o aux vai tomar a posição da raiz e portante assume o seu filho direito */
		}
	}

	else if (root->right == NULL && root->left == NULL)	/* caso o node a apagar não tenha nenhum filho */
		aux = NULL;

	else if (root->right != NULL)						/* caso tenha apenas o filho direito */
		aux = root->right;

	else
		aux = root->left;								/* caso tenha apenas o filho esquerdo */

	deleteNode(root);
	return aux;
}

void deleteNode (Link root)
{
	free(root->lines);						/* liberta a memória alocada para a palavra, o vector das linhas, */
	free(root->word);						/* o vector com posições e a estrutura */
	free(root->positions);	
	free(root);
}

void deleteTree (Link root)
{
	if (root == NULL)
		return;

	deleteTree(root->left);					/* liberta a memória alocada para a árvore */
	deleteTree(root->right);
	deleteNode(root);
}

Link maxLeft (Link root, Link temp)
{
	if (root->right->right == NULL)
		return root;							/* encontra o pai do maior filho do ramo esquerdo */

	temp = root;

	return maxLeft(root->right, temp);
}

Link lookUp (Link root, Word word)
{

	if (root == NULL)
		return root;

	else if (!strcmp(root->word, word))			/* compara a palavra com o node em que está */
		return root;

	else if (strcmp(root->word, word) > 0)			/* se for menor então está no ramo esquerdo */
		return lookUp (root->left, word);

	else
		return lookUp (root->right, word);				/* se for maior então está no ramo direito */
}

Link updateDepth (Link root)
{
	if (root == NULL)
		return root;

	if(root->right == NULL && root->left == NULL)					/* determina a profundidade de um node com base na dos seus filhos */
		root->depth = 0;

	else if(root->right == NULL && root->left != NULL)
		root->depth = root->left->depth + 1;

	else if(root->left == NULL && root->right != NULL) 				/* a profundidade de um nó com dois filhos é a maior das profundidades dos filhos mais 1 */
		root->depth = root->right->depth + 1;

	else
		root->depth = (root->right->depth > root->left->depth) ? (root->right->depth+1) : (root->left->depth+1);

	return root;
}

int balanceFactor(Link root)
{
	if(root->left == NULL && root->right == NULL) 
		return 0;

	else if(root->left == NULL)
		return -(root->right->depth) -1;			/* retorna o balance factor que é calculado com base na profundidade dos filhos */

	else if(root->right == NULL)
		return root->left->depth +1;

	else
		return ((root->left->depth) - (root->right->depth));
}

Link testBranch (Link root)
{
	int bfactor;

	if (root != NULL && abs(bfactor = balanceFactor(root)) > 1)			/* testa se o node está desiquilibrado ( |bfactor| > 1 ) */
	{
		root = balanceTree(root, bfactor);								/* equililibra a árvore */
		wasBalanced = 1;											/* sinaliza que a árvore foi equilibrada */
		return updateDepth(root);	
	}

	return root;
}


Link balanceTree (Link root, int bfactor)
{
	if (bfactor < -1)										/* se o ramo desiquilibrado é o da direita */
		{
			if (balanceFactor (root->right) >= 1)			/* caso haja um "cotovelo" com abertura para a direita */
				return doubleRotateRL (root);			

			else 
				return rotateLeft (root);					/* caso haja uma recta com declive negativo */
		}
	else 														/* se o ramo desiquilibrado é o da esquerda */
		{
			if (balanceFactor (root->left) <= -1 )				/* caso haja um "cotovelo" com abertura para a esquerda */
				return doubleRotateLR (root);

			else 
				return rotateRight (root);						/* caso haja uma recta com declive positivo  */
		}
}

Link doubleRotateRL (Link root)
{
	root->right = rotateRight(root->right);				/* rotaçao dupla direita - esquerda */
	return rotateLeft(root);
}

Link doubleRotateLR (Link root)
{
	root->left = rotateLeft(root->left);				/* rotaçao dupla esquerda - direita */
	return rotateRight(root);	
}


Link rotateLeft (Link root)
{
	Link aux = root->right;					/* rotação simples esquerda */
	root->right = aux->left;
	aux->left = root;

	root = updateDepth (root);    			/* é preciso calcular a profundidade dos nodes rodados novamente */
	aux = updateDepth (aux);

	return aux;
}
	
Link rotateRight (Link root)
{										/* rotação simples direita */
	Link aux = root->left;
	root->left = aux->right;
	aux->right = root;

	root = updateDepth (root);			/* é preciso calcular a profundidade do nodes rodados novamente */
	aux = updateDepth (aux);

	return aux;

}
