#!/bin/sh

gen=_src/output
src=_src/source
wsdl=ClientAccess.wsdl

rm -rf $gen $src
mkdir -p $gen $src
wsimport -d $gen -extension -Xnocompile -Xendorsed -keep -s $src -verbose $wsdl -p nubisave.client -wsdllocation http://localhost:8080/Matchmaker/services/ClientAccess?wsdl

origdir=$PWD
cd $src
javac nubisave/client/*
rm nubisave/client/*.java
zip -r nubisaveclient.jar nubisave

mv nubisaveclient.jar $origdir
