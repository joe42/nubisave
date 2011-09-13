#!/bin/sh

mkdir -p ~/.cache/nubisave/storages/storage02/
sudo umount ~/.cache/nubisave/storages/storage02/ 2>/dev/null
python -m cloudfusion.main ~/.cache/nubisave/storages/storage02/ sugarsync Nubisave123 cache
