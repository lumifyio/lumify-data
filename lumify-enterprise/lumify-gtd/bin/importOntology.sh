#!/bin/bash

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

classpath=$(${DIR}/classpath.sh .)
if [ $? -ne 0 ]; then
  echo "${classpath}"
  exit
fi

[ "${DEBUG_PORT}" ] || DEBUG_PORT=12345
[ "$1" = '-d' ] && debug_option="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=${DEBUG_PORT}"

java ${debug_option} \
-Dfile.encoding=UTF-8 \
-classpath ${classpath} \
-Xmx1024M \
io.lumify.core.cmdline.OwlImport \
--in=${DIR}/../ontology/gtd.owl \
--iri=http://lumify.io/gtd
