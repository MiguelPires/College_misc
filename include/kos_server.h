#include <buffer.h>
#include <delay.h>

int server_thread_init (int num_server_threads);
void *read_request ();
