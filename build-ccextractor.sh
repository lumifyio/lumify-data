#!/bin/bash -e

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

source ${DIR}/setenv.sh
source ${DIR}/functions.sh


name="lumify-ccextractor"
version="0.66"
release="dist"

_download \
    ${name} \
    http://downloads.sourceforge.net/project/ccextractor/ccextractor/${version}/ccextractor.src.${version}.zip \
    ccextractor.src.${version}.zip

if [ -d ${SOURCE_DIR}/ccextractor.${version} ]; then
  mv ${SOURCE_DIR}/ccextractor.${version} ${SOURCE_DIR}/${name}
fi

_build ${name} ${version} ${release} x86_64 i386
