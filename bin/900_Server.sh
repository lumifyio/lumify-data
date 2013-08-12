#!/bin/bash
# require: 215_ArtifactFaceDetectionMR.sh
# require: 215_VideoFrameFaceDetectionMR.sh
# require: 300_VideoPreviewMR.sh
# require: 710_TermMentionToTitanMR.sh
# require: 600_ArtifactLocationExtractionMR.sh
# require: 790_ArtifactHighlighting.sh
# require: 800_BlurSearchIndexBuilder.sh

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
  ip=$(ifconfig eth0 | awk -F ':| +' '/inet addr/ {print $4}')
else
  ip=192.168.33.10
fi

java \
-Dfile.encoding=UTF-8 \
-classpath ${classpath} \
-Xmx1024M \
com.altamiracorp.reddawn.web.Server \
--zookeeperInstanceName=reddawn \
--blurControllerLocation=${ip}:40010 \
--blurPath=hdfs://${ip}/blur \
--graph.storage.index.search.hostname=${ip} \
--hadoopUrl=hdfs://${ip}:8020 \
--zookeeperServerNames=${ip} \
--username=root \
--password=password \
