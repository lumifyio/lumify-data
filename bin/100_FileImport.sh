#!/bin/bash
# require: 075_FormatUcd.sh
# require: 080_Ontology.sh

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

classpath=$(${DIR}/classpath.sh analytics)
if [ $? -ne 0 ]; then
  echo "${classpath}"
  exit
fi

if [ "$2" != '' ]; then
  dir=$2
else
  dir=${DIR}/../data/import
fi

java \
-Dfile.encoding=UTF-8 \
-classpath ${classpath} \
com.altamiracorp.lumify.cmdline.FileImport \
--directory=${dir} \
--zipfile=$1
