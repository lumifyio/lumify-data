#!/bin/bash

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

classpath=$(${DIR}/classpath.sh core)
[ $? -eq 0 ] || echo "${classpath}" && exit

java \
-Dfile.encoding=UTF-8 \
-classpath ${classpath} \
com.altamiracorp.reddawn.textExtraction.TextExtractionMR \
--zookeeperInstanceName=reddawn \
--zookeeperServerNames=192.168.33.10 \
--username=root \
--password=password \
--classname=com.altamiracorp.reddawn.textExtraction.TikaTextExtractor
