# jerasure/Examples/Makefile
CC      = gcc
CFLAGS  = -g -Wall -I../include
LDFLAGS = -L../lib
LDLIBS  = -lJerasure
SRCS    = $(wildcard *.c)
OBJS    = $(SRCS:.c=.o)
TARGETS = $(SRCS:.c=)

all: $(TARGETS)

compile: lib $(OBJS)

clean:
	$(RM) $(RMFLAGS) $(OBJS) $(TARGETS) 

lib:
	$(MAKE) -C ../src/

