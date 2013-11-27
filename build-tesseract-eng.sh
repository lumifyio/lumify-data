#!/bin/bash -ex

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

source ${DIR}/setenv.sh
source ${DIR}/functions.sh


name="lumify-tesseract-eng"
version="3.02"
release="dist"

_download \
    ${name} \
    https://tesseract-ocr.googlecode.com/files/tesseract-ocr-${version}.eng.tar.gz \
    tesseract-${version}-eng.tar.gz

if [ -d ${SOURCE_DIR}/tesseract-ocr ]; then
  mv ${SOURCE_DIR}/tesseract-ocr ${SOURCE_DIR}/${name}
fi

_build ${name} ${version} ${release}
