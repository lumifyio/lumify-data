#!/bin/bash

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

classpath=$(${DIR}/../../../bin/classpath.sh lumify-enterprise/lumify-opennlp-dictionary-extractor)
if [ $? -ne 0 ]; then
  echo "${classpath}"
  exit
fi

java \
-Dfile.encoding=UTF-8 \
-classpath ${classpath} \
io.lumify.opennlpDictionary.DictionaryImporter \
--configLocation file:///opt/lumify/config/configuration.properties \
--extension=dict \
--directory=${DIR}/../config/dictionaries \

