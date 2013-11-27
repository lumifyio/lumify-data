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


name="lumify-theora"
version="1.1.1"
release="dist"

_download \
    ${name} \
    http://downloads.xiph.org/releases/theora/libtheora-${version}.tar.gz \
    libtheora-${version}.tar.gz

if [ -d ${SOURCE_DIR}/libtheora-${version} ]; then
  mv ${SOURCE_DIR}/libtheora-${version} ${SOURCE_DIR}/${name}
fi

_build ${name} ${version} ${release}
