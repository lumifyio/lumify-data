#!/bin/bash

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

if [ "$1" != '' ]; then
  dir=$1
else
  dir=${DIR}/../data/import
fi

if [ "${VIRTUALIZATION_DISABLED}" = 'true' ]; then
  ip=$(ifconfig eth0 | grep 'inet addr:' | cut -d: -f2 | awk '{ print $1}')
else
  ip=192.168.33.10
fi

java \
-Dfile.encoding=UTF-8 \
-classpath ${classpath} \
com.altamiracorp.reddawn.cmdline.WikipediaSearch $*
