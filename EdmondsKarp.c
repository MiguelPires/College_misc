#include <stdio.h>
#include <stdlib.h>
#define TRUE 1
#define FALSE 0
#define EDGE_CAPACITY 1
#define INFINITE -1
/* Estruturas de dados */

/*arestas*/
typedef struct edge_s* Edge;
struct edge_s
{
	int targetNode, flow, residualCapacity;
	Edge next;
};

/*vertices*/
typedef struct node* Link;
struct node 
{ 
    int value;
    char color;
    Link parent, next, prev;
}; 

typedef struct queue_s* Queue;
struct queue_s
{
	Link start, end;
};

/* variaveis globais */
Link *vertices;					/*vector com os vertices do grafo*/
Edge *edges;					/* vector uma lista para as arestas de cada vertice*/
Queue queue;
int numLocations, numConnections;

void enqueue (Link node)
{
	Link aux = queue->start;
	node->next = aux;
	queue->start = node;

	if (node->next != NULL)
		node->next->prev = node;

	else 
		queue->end = node;
}

Link dequeue ()
{
	if (queue->end == NULL)
		return NULL;

	Link aux = queue->end;
	queue->end = aux->prev;

	if (aux->prev == NULL)
		queue->start = NULL;
	
	return aux;
}

/* codigo das listas */
Link newNode (int value)
{
	Link l = (Link) malloc (sizeof(struct node));
	l->value = value;
	l->next = NULL;
	l->parent = NULL;
	l->color = 'w';
	return l;
}

Edge newEdge (int value)
{
	Edge n = (Edge) malloc (sizeof (struct edge_s));
	n->targetNode = value;
	n->next = NULL;
	n->flow = 0;
	n->residualCapacity = 1;

	return n;
}

void insertEdge (int fromNode, int toNode) 
{
	Edge aux, head = edges[fromNode];

	aux = newEdge (toNode);
	aux->next = head;
	edges [fromNode] = aux;

	/* symmetric edge in the residual network*/
	head = edges[toNode];

	aux = newEdge (fromNode);
	aux->next = head;
	edges [toNode] = aux;
}

int BFS_visit (Link parent, Link child, Link target)
{
	child->parent = parent;
	child->color = 'b';
		
	if (child == target)
		return 1;

	enqueue (child);

	return 0;
}

int BFS (Link source, Link target)
{
	Link aux, adj;
	Edge edge;
	int i;

	queue->start = NULL;
	queue->end = NULL;
	
	for (i = 0; i < numLocations; ++i)
	{
		vertices[i]->color = 'w';
		vertices[i]->parent = NULL;	
		vertices[i]->next = NULL;
		vertices[i]->prev = NULL;
	}
	source->color = 'b';

	for (aux = source; aux != NULL; aux = dequeue ())
		for (edge = edges[aux->value]; edge != NULL; edge = edge->next)
		{	
			adj = vertices [edge->targetNode];
		
			if (edge->residualCapacity == 0 || adj->color == 'b')
				continue;

			else if (BFS_visit (aux, adj, target))										/*target found*/
				return 1;
		}
	
	return 0;
}

void updateSymmetricEdge (Edge edge, Link parent, Link child)
{
	Edge symEdge;

	for (symEdge = edges [child->value]; vertices [symEdge->targetNode] != parent; symEdge = symEdge->next); /* encontra a aresta simetrica*/
	symEdge->residualCapacity = edge->flow;										/* actualiza a capacidade residual da aresta simetrica*/
	symEdge->flow = 0;
}

void augmentPath (Link source, Link target, int pathCapacity)
{
	Link child, parent;
	Edge aux;

	for (child = target, parent = child->parent; child != source; child = parent, parent = child->parent) 
	{			
		for (aux = edges [parent->value]; vertices [aux->targetNode] != child; aux = aux->next); /* encontra a edge*/
		aux->flow += pathCapacity;
		aux->residualCapacity = EDGE_CAPACITY - aux->flow;
		updateSymmetricEdge (aux, parent, child);
	}
}

int EdmondsKarp (Link source, Link target)
{
	int pathCapacity = INFINITE;
	int minCut = 0;
	Link child, parent;
	Edge edge, reverseEdge;

	while (1)
	{
		if (BFS (source, target))
		{
			for (child = target, parent = child->parent; child != source; child = parent, parent = child->parent) 
			{			
				for (edge = edges [parent->value]; vertices [edge->targetNode] != child; edge = edge->next); /* encontra a edge*/
				if (pathCapacity > edge->residualCapacity || pathCapacity == INFINITE) pathCapacity = edge->residualCapacity;
			}

			augmentPath (source, target, pathCapacity);
		}
		else
		{
			for (edge = edges [target->value]; edge != NULL; edge = edge->next)
			{
				for (reverseEdge = edges [edge->targetNode]; reverseEdge->targetNode != target->value; reverseEdge = reverseEdge->next); /* encontra a edge*/
				minCut += reverseEdge->flow;
			}

			return minCut;
		}		
	}
}

int main ()
{
	int numProblems, numCriticalPoints, fromNode, targetNode;
	int *criticalPointsList, unused;
	int i, e, j, d, minCut = INFINITE;
	int tempCut = 0;
	Edge edge;

	queue = (Queue) malloc (sizeof(struct queue_s));

	unused = scanf("%d %d", &numLocations, &numConnections);
	vertices = (Link*) malloc (numLocations*sizeof(Link));
	edges = (Edge*) malloc (numLocations*sizeof(Edge));

	for (i = 0; i < numLocations; ++i)
		vertices [i] = newNode (i);

	for (i = 0; i < numConnections; ++i)
	{
		unused = scanf ("%d %d", &fromNode, &targetNode);
		insertEdge (fromNode, targetNode);
	}	

	unused = scanf("%d", &numProblems);

	for (i = 0; i < numProblems; ++i)
	{
		unused = scanf("%d", &numCriticalPoints);
		criticalPointsList = (int*) malloc (sizeof (int)*numCriticalPoints);

		for (e = 0; e < numCriticalPoints; ++e)
			unused = scanf ("%d ", criticalPointsList+e);

		for (e = 0; e < numCriticalPoints; ++e)
			for (j = 0; j < numCriticalPoints; ++j)
				if (e > j)
				{
					tempCut = EdmondsKarp (vertices [criticalPointsList[j]], vertices [criticalPointsList[e]]);	
					
					if (minCut > tempCut || minCut == INFINITE)
						minCut = tempCut;

					for (d = 0; d < numLocations; ++d)
						for (edge = edges[d]; edge != NULL; edge = edge->next)
						{
							 edge->flow = 0;
							 edge->residualCapacity = 1;
						}
				}

		printf("%d\n", minCut);
		minCut = INFINITE;
	}
	++unused;
	return 0;
}