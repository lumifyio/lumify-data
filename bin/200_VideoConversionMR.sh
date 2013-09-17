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

ffmpegDir=$(dirname $(which ffmpeg))
ccextractorDir=$(dirname $(which ccextractor))

java \
-Dfile.encoding=UTF-8 \
-classpath ${classpath} \
com.altamiracorp.lumify.videoConversion.VideoConversionMR \
--failOnFirstError \
-Dffmpeg.bin.dir=${ffmpegDir} \
-Dccextractor.bin.dir=${ccextractorDir}