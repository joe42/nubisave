#!/bin/sh

# Usage: ./mount.sh mountpointfolder storagesfolder

. ../fusej/build.conf

find $2 -type d -exec fusermount -u {} -z \; 
fusermount -u $1 -z;
mkdir -p ~/.splitter/db/ #create log director

LD_LIBRARY_PATH=../fusej/jni:$FUSE_HOME/lib $JDK_HOME/bin/java \
   -classpath ./build:./lib/*:../jigdfs/lib/*:../fusej/lib/* \
   -Dorg.apache.commons.logging.Log=fuse.logging.FuseLog \
   -Dfuse.logging.level=INFO \
   com.github.joe42.splitter.Main -f -s -obig_writes,max_read=131072,max_write=131072 $1 $2

find $2 -type d -exec fusermount -u {} -z \; 
fusermount -u $1 -z;


