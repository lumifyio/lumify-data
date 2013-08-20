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
  ip=$(ifconfig eth0 | awk -F ':| +' '/inet addr/ {print $4}')
else
  ip=192.168.33.10
fi

if [ "$1" != '' ]; then
  filename=$1
  admin1codeFilename=$2
  countryInfoFilename=$3
else
  filename=${DIR}/../data/allCountries.txt
  admin1codeFilename=${DIR}/../data/admin1CodesASCII.txt
  countryInfoFilename=${DIR}/../data/countryInfo.txt
fi

java \
-Dfile.encoding=UTF-8 \
-classpath ${classpath} \
com.altamiracorp.reddawn.location.GeoNamesImport \
--zookeeperInstanceName=reddawn \
--zookeeperServerNames=${ip} \
--blurControllerLocation=${ip}:40010 \
--blurPath=hdfs://${ip}/blur \
--graph.storage.index.search.hostname=${ip} \
--hadoopUrl=hdfs://${ip}:8020 \
--username=root \
--password=password \
--filename=${filename} \
--admin1code=${admin1codeFilename} \
--countryinfo=${countryInfoFilename}
