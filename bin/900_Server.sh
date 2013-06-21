#!/bin/bash

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

classpath=$(${DIR}/classpath.sh web)
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
com.altamiracorp.reddawn.web.Server \
--zookeeperInstanceName=reddawn \
--blurControllerLocation=${ip}:40010 \
--blurPath=hdfs://${ip}/blur \
--zookeeperServerNames=${ip} \
--username=root \
--password=password \
