#!/bin/bash
# group: server
# require: 215_ArtifactFaceDetectionMR.sh
# require: 215_VideoFrameFaceDetectionMR.sh
# require: 300_VideoPreviewMR.sh
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
-Djava.security.krb5.realm= \
-Djava.security.krb5.kdc= \
-classpath ${classpath} \
-Xmx1024M \
com.altamiracorp.lumify.web.Server \
--port=8080 \