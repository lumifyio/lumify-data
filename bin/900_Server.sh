#!/bin/bash

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

classpath=$(${DIR}/classpath.sh lumify-public/lumify-web-server/lumify-jetty-server)
if [ $? -ne 0 ]; then
  echo "${classpath}"
  exit
fi

[ "${DEBUG_PORT}" ] || DEBUG_PORT=12345
[ "$1" = '-d' ] && debug_option="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=${DEBUG_PORT}"

cd ${DIR}/../lumify-public

java ${debug_option} \
-Dfile.encoding=UTF-8 \
-Djava.awt.headless=true \
-Djava.security.krb5.realm= \
-Djava.security.krb5.kdc= \
-classpath ${classpath} \
-Xmx1024M \
com.altamiracorp.lumify.web.JettyWebServer \
--port=8080 \
--keyStorePath=${DIR}/../config/ssl/lumify-vm.lumify.io.jks \
--keyStorePassword=password
