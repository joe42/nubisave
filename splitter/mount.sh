#!/bin/sh

# Usage: ./mount.sh mountpointfolder storagesfolder

. ../fusej/build.conf

find $2 -type d -exec fusermount -u {} -z \; 
fusermount -u $1 -z;
mkdir -p ~/.splitter/db/ #create log director

LD_LIBRARY_PATH=../fusej/jni:$FUSE_HOME/lib $JDK_HOME/bin/java \
   -classpath ./build:./lib/*:../jigdfs/lib/*:../fusej/lib/* \
   -Dorg.apache.commons.logging.Log=fuse.logging.FuseLog \
   -Dfuse.logging.level=DEBUG \
   com.github.joe42.splitter.FuseBox -f -s $1 $2

find $2 -type d -exec fusermount -u {} -z \; 
fusermount -u $1 -z;


