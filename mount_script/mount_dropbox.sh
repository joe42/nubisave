#!/bin/sh

mkdir -p ~/.cache/nubisave/storages/storage01/
sudo umount ~/.cache/nubisave/storages/storage01/ 2>/dev/null
python -m cloudfusion.main ~/.cache/nubisave/storages/storage01/ dropbox nubisave123 cache
