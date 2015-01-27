#!/bin/sh

_file=$1

echo "read start fiLe $_file"

_dir=`dirname "$1"`
 
echo $_dir

echo 'changing to' $_dir

cd $_dir

echo 'executing ' $_file

chmod u+x $_dir/*.sh

export JRE_HOME=/opt/jre1.7.0_05/


echo "Using JRE_HOME: $JRE_HOME"

exec "$_file" start "$@"
#exec "$_file" 
