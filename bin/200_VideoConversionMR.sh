#!/bin/bash
# group: video
# require: 150_ContentTypeExtractionMR.sh

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

ffmpegDir=$(dirname $(which ffmpeg))
ccextractorDir=$(dirname $(which ccextractor))

java \
-Dfile.encoding=UTF-8 \
-classpath ${classpath} \
com.altamiracorp.reddawn.videoConversion.VideoConversionMR \
--zookeeperInstanceName=reddawn \
--zookeeperServerNames=${ip} \
--hadoopUrl=hdfs://${ip}:8020 \
--blurControllerLocation=${ip}:40010 \
--blurPath=hdfs://${ip}/blur \
--graph.storage.index.search.hostname=${ip} \
--username=root \
--password=password \
--failOnFirstError \
-Dffmpeg.bin.dir=${ffmpegDir} \
-Dccextractor.bin.dir=${ccextractorDir}
