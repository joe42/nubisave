#!/bin/sh

cd splitter

. ./build.conf

mkdir -p ~/.cache/nubisave/storages/storage01/
mkdir -p ~/.cache/nubisave/storages/storage02/
mkdir -p ~/.cache/nubisave/storages/storage03/
mkdir -p ~/nubisave

LD_LIBRARY_PATH=./jni:$FUSE_HOME/lib $JDK_HOME/bin/java \
   -classpath ./build:./lib/* \
   -Dorg.apache.commons.logging.Log=fuse.logging.FuseLog \
   -Dfuse.logging.level=DEBUG \
   fuse.zipfs.ZipFilesystem -f -s ~/nubisave ~/.cache/nubisave/storages
