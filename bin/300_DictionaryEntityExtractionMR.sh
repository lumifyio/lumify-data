#!/bin/bash

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

classpath=$(${DIR}/classpath.sh core)
if [ $? -ne 0 ]; then
  echo "${classpath}"
  exit
fi

if [ "${VIRTUALIZATION_DISABLED}" = 'true' ]; then
  ip=localhost
else
  ip=192.168.33.10
fi

java \
-Dfile.encoding=UTF-8 \
-classpath ${classpath} \
-Xmx1g \
-XX:MaxPermSize=512m \
com.altamiracorp.reddawn.entityExtraction.EntityExtractionMR \
--zookeeperInstanceName=reddawn \
--zookeeperServerNames=${ip} \
--blurControllerLocation=${ip}:40010 \
--blurPath=hdfs://${ip}/blur \
--hadoopUrl=hdfs://${ip}:8020 \
--username=root \
--password=password \
--classname=com.altamiracorp.reddawn.entityExtraction.OpenNlpDictionaryEntityExtractor \
--config=nlpConfPathPrefix=file://$(cd ${DIR}/.. && pwd)
