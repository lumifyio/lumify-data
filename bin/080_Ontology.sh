#!/bin/bash

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

classpath=$(${DIR}/classpath.sh lumify-enterprise/lumify-enterprise-tools)
if [ $? -ne 0 ]; then
  echo "${classpath}"
  exit
fi

in=${DIR}/../lumify-public/lumify-ontology-dev/ontology/dev.owl
iri=http://lumify.io/dev
if [ $# -eq 2 ]; then
  in=$1
  iri=$2
fi

java \
-Dfile.encoding=UTF-8 \
-classpath ${classpath} \
-Xmx1024M \
com.altamiracorp.lumify.core.cmdline.OwlImport \
--in=${in} \
--iri=${iri}
