#include <stdio.h>
#include <stdlib.h>
#define TRUE 1
#define FALSE 0

/* Estruturas de dados */

/*arestas*/
typedef struct edge_s* edge;
struct edge_s
{
	int targetNode;
	edge next;
};

/*vertices*/
typedef struct node* link;
struct node 
{ 
    int value, depth, min, groupId;
    link next;
}; 

/*elementos da stack*/
typedef struct stack_s* stackNode;
struct stack_s
{
	link node;	
	stackNode next;
};


/*SCC*/
typedef struct scc_s *SCC;
struct scc_s
{
	SCC next;							/* proximo componente fortemente ligado */
	link nodes;							/* vertices no componente*/
	int groupId;						/* identificador de grupo */
};


/* variaveis globais */
link *vertices;					/*vector com os vertices do grafo*/
edge *edges;					/* vector uma lista para as arestas de cada vertice*/
int maximumGroupSize, lonerGroups, groupCount, *stackContains, depth = 0;
stackNode stackTop;
SCC components = NULL;

/* codigo das listas */
link newNode (int value)
{
	link l = (link) malloc (sizeof(struct node));
	l->value = value;
	l->depth = -1;
	l->min = -1;
	return l;
}

edge newEdge (int value)
{
	edge n = (edge) malloc (sizeof (struct edge_s));
	n->targetNode = value;
	n->next = NULL;
	return n;
}

/* stack */
stackNode newStackNode (link newNode) 
{
	stackNode new = (stackNode) malloc (sizeof (struct stack_s));
	new->node = newNode;
	new->next = NULL;
	return new;

}
void push (link newNode)
{
	stackNode sNode = newStackNode (newNode);
	sNode->next = stackTop;
	stackTop = sNode;
}

stackNode pop ()
{
	stackNode aux = stackTop; 
	stackTop = aux->next;
	return aux;
}

/* codigo do grafo*/

void searchGroup (link vertex, int index)
{
	int tempMaxSize;
	edge e;
	link node = NULL;
	SCC aux;

	stackNode sNode = NULL;
	tempMaxSize = 0;

	vertex->depth = depth;
	vertex->min = depth;
	++depth;
	push (vertex);

	for (e = edges [index]; e != NULL; e = e->next)
	{
		node = vertices [e->targetNode-1];
	
		if (node->depth == -1)									/* se o no' ainda nao tiver sido explorado*/
		{
			searchGroup (node, e->targetNode-1);	

			if (vertex->min > node->min)						/*substitui o min do no' se o no' que o sucede tem menor min (encontrou um arco para tras) */
				vertex->min = node->min;
		}
		else if (stackContains[node->value-1])								
			if (node->depth < vertex->min)						
				vertex->min = node->depth;
	}

	if (vertex->depth == vertex->min)								/* quando vertex e' a raiz */
	{
		if (components == NULL)
		{
			components = (SCC) malloc (sizeof (struct scc_s));
			components->next = NULL;
			aux = components;
		}															/* cria um novo SCC */

		else
		{			
			aux = (SCC) malloc (sizeof (struct scc_s));
			aux->next = components->next;
			components->next = aux;
		}

		aux->groupId = groupCount;

		do
		{		
			sNode = pop ();										/*retira um vertice da stack */
			stackContains[sNode->node->value-1] = FALSE;
			++tempMaxSize;
			sNode->node->groupId = groupCount;

			if (tempMaxSize == 1) 
			{
				aux->nodes = sNode->node;
				node = sNode->node;
			}													/* guarda os vertices do SCC na estrutura*/

			else 
			{
				node->next = sNode->node;
				node = node->next;
			}

		} while (sNode->node != vertex);								/* quando chega 'a raiz na stack, retirou todos vertices do grupo*/

		++groupCount;													/*aumenta o numero de grupos */

		if (tempMaxSize > maximumGroupSize)								/*compara o tamanho do grupo e o maior encontrado ate ao momento*/
			maximumGroupSize = tempMaxSize;
	}
}

void search (int numPeople)
{
	int i, currentGroup, ignoreSCC = FALSE;
	edge e;
	SCC aux;
	link node;

	for (i = 0; i < numPeople; ++i)
		if (vertices [i]->depth == -1)		
			searchGroup (vertices [i], i); 							/*so o no' ja e' conhecido, nao explora esse caminho */

	lonerGroups = groupCount;

	for (aux = components; aux != NULL; aux = aux->next)
	{
		currentGroup = aux->groupId;

		for (node = aux->nodes; node != NULL; node = node->next)
		{
			for (e = edges[node->value-1]; e != NULL; e = e->next)
				if (currentGroup != vertices[e->targetNode-1]->groupId)
				{
					--lonerGroups;
					ignoreSCC = TRUE;
					break;
				}

			if (ignoreSCC)
				break;
		}

		ignoreSCC = FALSE;
	}
}

void insert (int value, int newValue) 
{
	edge aux, head = edges[value-1];

	aux = newEdge (newValue);
	aux->next = head;

	edges [value-1] = aux;
}

int main()
{
	int numPeople, numShares, i, fromNode, targetNode;

	scanf("%d %d", &numPeople, &numShares);
	vertices = (link*) malloc (numPeople*sizeof(link));
	stackContains = (int*) malloc (numPeople*sizeof(int));
	edges = (edge*) calloc (numPeople, sizeof(edge));
	stackTop = (stackNode) malloc (sizeof (struct stack_s));


	for (i = 0; i < numPeople; ++i)
	{
		vertices [i] = newNode (i+1);
		stackContains [i] = TRUE;
	}

	for (i = 0; i < numShares; ++i)
	{
		scanf ("%d %d", &fromNode, &targetNode);
		insert (fromNode, targetNode);
	}

	groupCount = maximumGroupSize = lonerGroups = 0;
	search (numPeople);

	printf("%d\n%d\n%d\n", groupCount, maximumGroupSize, lonerGroups);
	return 0;
}