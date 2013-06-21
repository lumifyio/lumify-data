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
-Xmx1024M \
com.altamiracorp.reddawn.statementExtraction.StatementExtractionMR \
--zookeeperInstanceName=reddawn \
--zookeeperServerNames=${ip} \
--username=root \
--password=password \
--classname=com.altamiracorp.reddawn.statementExtraction.SentenceBasedStatementExtractor \
--config=nlpConfPathPrefix=file://$(cd ${DIR}/.. && pwd)
