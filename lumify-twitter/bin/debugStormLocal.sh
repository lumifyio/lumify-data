#!/bin/bash

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

classpath=$(${DIR}/classpath.sh .)
if [ $? -ne 0 ]; then
  echo "${classpath}"
  exit
fi

if [ "$DEBUG_PORT" == "" ]; then
	DEBUG_PORT=12345
fi

java \
-Xmx512m \
-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=$DEBUG_PORT \
-Dfile.encoding=UTF-8 \
-Djava.library.path=$LD_LIBRARY_PATH \
-classpath ${classpath} \
com.altamiracorp.lumify.twitter.storm.StormRunner \
--local \
$*
