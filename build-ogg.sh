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


name="lumify-ogg"
version="1.3.1"
release="dist"

_download \
    ${name} \
    http://downloads.xiph.org/releases/ogg/libogg-${version}.tar.gz \
    libogg-${version}.tar.gz

if [ -d ${SOURCE_DIR}/libogg-${version} ]; then
  mv ${SOURCE_DIR}/libogg-${version} ${SOURCE_DIR}/${name}
fi

_build ${name} ${version} ${release}
