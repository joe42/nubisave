PREFIX=/usr/lib/jvm/java-7-openjdk-amd64
LIBDIR=../bin/lib
INCDIR=${PREFIX}/include

all: lib/libJerasure.so lib/libJerasure.jni.so
	cp -P lib/libJerasure.jni.so.0 ${LIBDIR}/libJerasure.so

clean:
	make -C jni clean PREFIX=${PREFIX}
	make -C src clean
	rm -rf include

install: lib/libJerasure.so
	
	mkdir -p ${INCDIR}/jerasure
	cp include/* ${INCDIR}/jerasure

jni: lib/libJerasure.jni.so

lib/libJerasure.so:
	make -C src

lib/libJerasure.jni.so: lib/libJerasure.so
	make -C jni
