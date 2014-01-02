#!/bin/bash

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  CURRENTDIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$CURRENTDIR/$SOURCE"
done
CURRENTDIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

classpath=$(${CURRENTDIR}/classpath.sh crawler)
if [ $? -ne 0 ]; then
    echo "${classpath}"
    exit
fi

if [ $# -eq 0 ] || [ "$1" == "--help" ]
then
    java \
    -Dfile.encoding=UTF-8 \
    -classpath ${classpath} \
    com.altamiracorp.lumify.crawler.WebCrawl
    exit
fi

java \
-Dfile.encoding=UTF-8 \
-classpath ${classpath} \
com.altamiracorp.lumify.crawler.WebCrawl \
"${@}"
