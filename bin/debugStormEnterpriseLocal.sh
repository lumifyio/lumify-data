#!/bin/bash

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

classpath=$(${DIR}/classpath.sh lumify-enterprise/lumify-enterprise-storm)
if [ $? -ne 0 ]; then
  echo "${classpath}"
  exit
fi

if [ "$2" != '' ]; then
  dir=$2
else
  dir=${DIR}/../data/import
fi

if [ "$DEBUG_PORT" == "" ]; then
	DEBUG_PORT=12345
fi

java \
-Xmx512m \
-XX:MaxPermSize=128m \
-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=$DEBUG_PORT \
-Djava.awt.headless=true \
-Dfile.encoding=UTF-8 \
-Djava.library.path=$LD_LIBRARY_PATH \
-classpath ${classpath} \
com.altamiracorp.lumify.storm.StormEnterpriseRunner \
--datadir=${dir} \
--local
