#!/bin/bash

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

classpath=$(${DIR}/classpath.sh lumify-public/lumify-web)
if [ $? -ne 0 ]; then
  echo "${classpath}"
  exit
fi

cd ${DIR}/../lumify-public

java \
-Dfile.encoding=UTF-8 \
-Djava.awt.headless=true \
-Djava.security.krb5.realm= \
-Djava.security.krb5.kdc= \
-classpath ${classpath} \
-Xmx1024M \
com.altamiracorp.lumify.web.Server \
--port=8080 \
--keyStorePath=${DIR}/../conf/ssl/lumify.jks \
--keyStorePassword=password
