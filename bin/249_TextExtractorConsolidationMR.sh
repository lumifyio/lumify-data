#!/bin/bash
# require: 225_TextExtractionMR_OCR.sh
# require: 225_TextExtractionMR_Tika.sh
# require: 225_TextExtractionMR_Transcript.sh
# require: 225_TextExtractionMR_StructuredData.sh
# require: 230_TextExtractionMR_VideoFrameTextCombiner.sh

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
com.altamiracorp.lumify.textExtraction.TextExtractorConsolidationMR \
--failOnFirstError

