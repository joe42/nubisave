#!/bin/bash

echo "Start des Splitter Modules"
mountpoint=$HOME/.nubisave/nubisavemount
storages=$HOME/.nubisave/storages

find "$storages" -type d -exec fusermount -u {} -z \; 
fusermount -u "$mountpoint"  -z;
