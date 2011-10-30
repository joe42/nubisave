
include build.conf

JAVAC := javac
JAVA := java

BUILD_CLASSPATH := ./build:./lib/*:../jigdfs/lib/*:../fusej/lib/*

JAVASRC := $(shell find src -name '*.java') $(shell find ../jigdfs/src -name '*.java') $(shell find ../fusej/src -name '*.java')
JNI_BINDINGS_H := ../fusej/jni/javafs_bindings.h
JNI_BINDINGS_C := ../fusej/jni/javafs_bindings.c
JNI_COMMON_H := javafs.h


.PHONY: all java_all jni_all clean java_clean jni_clean

all: java_all jni_all
	@echo ""
	@echo "FUSE-J is built."
	@echo ""

clean: java_clean jni_clean


# Java part

java_all: ${JAVASRC}
	mkdir -p build
	${JAVAC} -Xlint -source 5 -classpath "${BUILD_CLASSPATH}" -d build ${JAVASRC}

java_clean:
	rm -rf build/*
	rm -rf splitter.db



# JNI part

jni_all: ${JNI_BINDINGS_H} ${JNI_BINDINGS_C} ../fusej/jvm_ldpath.def
	${MAKE} -C ../fusej/jni all

${JNI_BINDINGS_H} ${JNI_BINDINGS_C}: ${JAVASRC}
	${JAVA} -classpath build java2c.CAPIGenerator \
	${JNI_BINDINGS_H} ${JNI_BINDINGS_C} ${JNI_COMMON_H}

../fusej/jvm_ldpath.def:
	${JAVA} -classpath build java2c.DumpJVMLdPath > ../fusej/jvm_ldpath.def

jni_clean:
	${MAKE} -C ../fusej/jni clean
	rm -f ${JNI_BINDINGS_H} ${JNI_BINDINGS_C}
	rm -f ../fusej/jvm_ldpath.def



