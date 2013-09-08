#!/bin/sh
#
# Mounts the NubiSave FUSE module and all subordinate storage FUSE modules (and unmounts them to clean up)
# Usage: ./mount.sh <mountpointfolder> <storagesfolder>

# Don't assume build variables at runtime
#. ../fusej/build.conf

find $2 -mindepth 1 -type d -exec fusermount -u {} -z \;
fusermount -u $1 -z -q

if [ -d $JDK_HOME ] && [ -x $JDK_HOME/bin/java ]
then
	java=$JDK_HOME/bin/java
else
	java=java
fi

LD_LIBRARY_PATH=../fusej/jni:$FUSE_HOME/lib $java \
   -classpath ./build:./lib/*:../jigdfs/lib/*:../fusej/lib/* \
   -Dorg.apache.commons.logging.Log=fuse.logging.FuseLog \
   -Dfuse.logging.level=INFO \
   com.github.joe42.splitter.Main -f -s -obig_writes,max_read=131072,max_write=131072 $1 $2

find $2 -mindepth 1 -type d -exec fusermount -u {} -z \;
fusermount -u $1 -z
