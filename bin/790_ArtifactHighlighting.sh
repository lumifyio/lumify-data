#!/bin/bash
# require: 250_StructuredDataExtractorMR.sh
# require: 300_DictionaryEntityExtractionMR.sh
# require: 300_EntityExtractionMR.sh
# require: 300_KnownEntityExtractionMR.sh

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

java \
-Dfile.encoding=UTF-8 \
-classpath ${classpath} \
-Xmx1024M \
com.altamiracorp.lumify.entityHighlight.EntityHighlightMR \
--failOnFirstError
