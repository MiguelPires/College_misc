OBJECTS = kos_client.o kos_server.o kos.o kos_file.o buffer.o list.o delay.o hash.o 

DEFAULT_INCLUDES = -I.
INCLUDES = -I ../include
DEFS ?= -DHAVE_CONFIG_H #-DDEBUG
# -DC_DEBUG
CC = gcc
CFLAGS ?= -g -O0 -Wall -m32 -pthread

COMPILE = $(CC) $(DEFS) $(DEFAULT_INCLUDES) $(INCLUDES) $(CFLAGS) 

AR = ar
ARFLAGS = cru
RANLIB = ranlib

all: clean libkos.a

libkos.a: $(OBJECTS) 
	$(AR) $(ARFLAGS) $@ $^
	$(RANLIB) $@

.c.o:
	$(COMPILE) -c -o $@ $<

linkage: 
	$(COMPILE) -o main *.o

clean: 
	rm -f *.o *~ libkos.a

gdb:
	$(COMPILE) -c *.c
	make linkage
	gdb -q main

# Tell versions [3.59,3.63) of GNU make to not export all variables.
# Otherwise a system limit (for SysV at least) may be exceeded.
.NOEXPORT:
