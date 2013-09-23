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

classpath=$(${DIR}/classpath.sh analytics)
if [ $? -ne 0 ]; then
  echo "${classpath}"
  exit
fi

java \
-Djava.library.path=$LD_LIBRARY_PATH \
-Dfile.encoding=UTF-8 \
-classpath ${classpath} \
com.altamiracorp.lumify.objectDetection.ObjectDetectionMR \
--failOnFirstError \
--classname=com.altamiracorp.lumify.objectDetection.OpenCVObjectDetector \
-DopenCVConfPathPrefix=$(cd ${DIR}/.. && pwd) \
-Dclassifier.file=haarcascade_frontalface_alt.xml \
-Dclassifier.concept=face \
-DjobType=videoFrame