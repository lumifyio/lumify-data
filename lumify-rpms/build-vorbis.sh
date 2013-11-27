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


name="lumify-vorbis"
version="1.3.3"
release="dist"

_download \
    ${name} \
    http://downloads.xiph.org/releases/vorbis/libvorbis-${version}.tar.gz \
    libvorbis-${version}.tar.gz

if [ -d ${SOURCE_DIR}/libvorbis-${version} ]; then
  mv ${SOURCE_DIR}/libvorbis-${version} ${SOURCE_DIR}/${name}
fi

export QA_RPATHS=$[ 0x0001|0x0010 ]

_build ${name} ${version} ${release}
