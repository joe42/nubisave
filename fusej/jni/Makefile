include ../build.conf
include ../jvm_ldpath.def

SRCS := javafs.c javafs_bindings.c
HDRS := javafs.h javafs_bindings.h
LIB_SO := libjavafs.so

INCLUDES := -I${FUSE_HOME}/include -I${JDK_HOME}/include -I${JDK_HOME}/include/linux

LDPATH := ${LDPATH} -L${FUSE_HOME}/lib -L${JDK_HOME}/jre/lib/i386 -L${JDK_HOME}/jre/lib/i386/server -L${JDK_HOME}/jre/lib/amd64 -L${JDK_HOME}/jre/lib/amd64/server


all: ${LIB_SO}

${LIB_SO}: ${SRCS} ${HDRS}
	gcc -fPIC -shared -D_FILE_OFFSET_BITS=64 ${SRCS} -o ${LIB_SO} ${INCLUDES} ${LDPATH} -ljvm -lfuse -lpthread

clean:
	rm -f ${LIB_SO}

