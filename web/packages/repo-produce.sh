#!/bin/bash
#
# Moves built packages into a specific branch of the repository

pkgbranch=current
arch=`uname -m`

path=../../..

mkdir -p packages/$pkgbranch/{sources,binary-all}

cp $path/*.{dsc,tar.gz} packages/$pkgbranch/sources/
cp $path/*_all.deb packages/$pkgbranch/binary-all/
