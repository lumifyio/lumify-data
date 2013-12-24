#!/bin/bash
# require: 100_FileImport.sh

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

classpath=$(${DIR}/classpath.sh lumify-enterprise-tools)
if [ $? -ne 0 ]; then
  echo "${classpath}"
  exit
fi

java \
-Dfile.encoding=UTF-8 \
-classpath ${classpath} \
com.altamiracorp.lumify.entityExtraction.DictionaryImporter \
--configLocation file:///opt/lumify/config/configuration.properties \
--extension=dict \
--directory=${DIR}/../config/opennlp/dictionaries \

