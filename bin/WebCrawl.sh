#!/bin/bash

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

classpath=$(${DIR}/classpath.sh crawler)
if [ $? -ne 0 ]; then
  echo "${classpath}"
  exit
fi

if [ "$1" != '' ]; then
  dir=$1
else
  dir=${DIR}/../data
fi

java \
-Dfile.encoding=UTF-8 \
-classpath ${classpath} \
com.altamiracorp.reddawn.WebCrawl \
--directory=${dir} \
--provider=google \
--query="boston bombing"