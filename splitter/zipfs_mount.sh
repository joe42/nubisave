#!/bin/sh

# Usage: ./zipfs_mount.sh file.zip /mount/point

. ../fusej/build.conf

LD_LIBRARY_PATH=../fusej/jni:$FUSE_HOME/lib $JDK_HOME/bin/java \
   -classpath ./build:./lib/*:../jigdfs/lib/*:../fusej/lib/* \
   -Dorg.apache.commons.logging.Log=fuse.logging.FuseLog \
   -Dfuse.logging.level=DEBUG \
   com.github.joe42.splitter.Splitter -f -s $1 $2 $3
