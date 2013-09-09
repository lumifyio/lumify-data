#!/bin/bash
# group: video
# require: 200_VideoConversionMR.sh

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

java \
-Djava.library.path=$LD_LIBRARY_PATH \
-Dfile.encoding=UTF-8 \
-classpath ${classpath} \
com.altamiracorp.lumify.objectDetection.ObjectDetectionMR \
--zookeeperInstanceName=lumify \
--zookeeperServerNames=${ip} \
--blurControllerLocation=${ip}:40010 \
--blurPath=hdfs://${ip}/blur \
--graph.storage.index.search.hostname=${ip} \
--hadoopUrl=hdfs://${ip}:8020 \
--username=root \
--password=password \
--failOnFirstError \
--classname=com.altamiracorp.lumify.objectDetection.OpenCVObjectDetector \
-DopenCVConfPathPrefix=file://$(cd ${DIR}/.. && pwd) \
-Dclassifier.file=haarcascade_frontalface_alt.xml \
-Dclassifier.concept=face \
-DjobType=videoFrame
