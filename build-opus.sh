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


name="lumify-opus"
version="1.0.3"
release="dist"

_download \
  ${name} \
  http://downloads.xiph.org/releases/opus/opus-${version}.tar.gz \
  opus-${version}.tar.gz

if [ -d ${SOURCE_DIR}/opus-${version} ]; then
  mv ${SOURCE_DIR}/opus-${version} ${SOURCE_DIR}/${name}
fi

_build ${name} ${version} ${release}
