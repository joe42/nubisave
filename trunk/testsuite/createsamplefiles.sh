#!/bin/bash

mkdir samplefiles

cd samplefiles

echo "create samplefiles"

dd if=/dev/urandom of=500kb.0 bs=1K count=500
dd if=/dev/urandom of=500kb.1 bs=1K count=500
dd if=/dev/urandom of=500kb.2 bs=1K count=500

dd if=/dev/urandom of=1mb.0 bs=1M count=1
dd if=/dev/urandom of=1mb.1 bs=1M count=1
dd if=/dev/urandom of=1mb.2 bs=1M count=1

dd if=/dev/urandom of=10mb.0 bs=1M count=10
dd if=/dev/urandom of=10mb.1 bs=1M count=10
dd if=/dev/urandom of=10mb.2 bs=1M count=10

dd if=/dev/urandom of=50mb.0 bs=1M count=50
dd if=/dev/urandom of=50mb.1 bs=1M count=50
dd if=/dev/urandom of=50mb.2 bs=1M count=50

echo "create checksums"
md5sum * > checksums
