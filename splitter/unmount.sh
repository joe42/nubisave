#!/bin/bash

echo "Unmount aller Storages"
mountpoint=$HOME/.nubisave/nubisavemount
storages=$HOME/.storages

find "$storages" -type d -exec fusermount -u {} -z \;
fusermount -u "$mountpoint"  -z;
