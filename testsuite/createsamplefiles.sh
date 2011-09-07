#!/bin/bash

mkdir -p samplefiles
rm samplefiles/*
cd samplefiles

echo "create samplefiles"

for N in 4 8 16 32 64 128 256 512 1024 2048 4096 8192 16384 32768 65536 131072
do
	dd if=/dev/urandom of=${N}kb bs=${N}K count=1
done

echo "create checksums"
md5sum * > checksums
