include ../build.conf
include ../jvm_ldpath.def

SRCS := javafs.c javafs_bindings.c
HDRS := javafs.h javafs_bindings.h
LIB_SO := libjavafs.so

INCLUDES := -I${FUSE_HOME}/include -I${JDK_HOME}/include -I${JDK_HOME}/include/linux

LDPATH := ${LDPATH} -L${FUSE_HOME}/lib -L/usr/lib/x86_64-linux-gnu/gcj-4.6-12/


all: ${LIB_SO}

${LIB_SO}: ${SRCS} ${HDRS}
	gcc -fPIC -shared -D_FILE_OFFSET_BITS=64 ${SRCS} -o ${LIB_SO} ${INCLUDES} ${LDPATH} -ljvm -lfuse -lpthread

clean:
	rm -f ${LIB_SO}

