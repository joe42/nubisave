#!/bin/sh
mkdir -p $1
mkdir -p .cloudfusion/logs
python -m cloudfusion.main $1
