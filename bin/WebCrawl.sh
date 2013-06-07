#!/bin/bash

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

classpath=$(${DIR}/classpath.sh crawler)
if [ $? -ne 0 ]; then
    echo "${classpath}"
    exit
fi

if [ $# -eq 4 ]
then
    PARAM3="$4"
elif [ $# -eq 3 ]
then
    PARAM3="--directory=${DIR}/../data"
else
    echo "You must supply provider and query options with an optional directory option"
    exit
fi

java \
-Dfile.encoding=UTF-8 \
-classpath ${classpath} \
com.altamiracorp.reddawn.WebCrawl \
"$1" \
"$2" \
"$3" \
"${PARAM3}"
