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


name="lumify-leptonica"
version="1.69"
release="dist"

_download \
    ${name} \
    http://www.leptonica.org/source/leptonica-${version}.tar.gz \
    leptonica-${version}.tar.gz

if [ -d ${SOURCE_DIR}/leptonica-${version} ]; then
  mv ${SOURCE_DIR}/leptonica-${version} ${SOURCE_DIR}/${name}
fi

_build ${name} ${version} ${release} x86_64 i386
