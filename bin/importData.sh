#!/bin/bash

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

classpath=$(${DIR}/classpath.sh lumify-enterprise/lumify-enterprise-storm)
if [ $? -ne 0 ]; then
  echo "${classpath}"
  exit
fi

dir=${DIR}/../data/import
if [ "$1" != '-d' ]; then
	[ "$1" ] && dir=$1 && shift
fi

[ "${DEBUG_PORT}" ] || DEBUG_PORT=12345
[ "$1" = '-d' ] && debug_option="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=${DEBUG_PORT}"

java ${debug_option} \
-Xmx512m \
-Djava.awt.headless=true \
-Dfile.encoding=UTF-8 \
-Djava.library.path=$LD_LIBRARY_PATH \
-classpath ${classpath} \
io.lumify.tools.Import \
--datadir=${dir} \
--queuedups

