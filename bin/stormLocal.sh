#!/bin/bash

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

classpath=$(${DIR}/classpath.sh storm)
if [ $? -ne 0 ]; then
  echo "${classpath}"
  exit
fi

if [ "$2" != '' ]; then
  dir=$2
else
  dir=${DIR}/../data/import
fi

java \
-Xmx512m \
-Dfile.encoding=UTF-8 \
-classpath ${classpath} \
com.altamiracorp.lumify.storm.StormRunner \
--datadir=${dir} \
--local
