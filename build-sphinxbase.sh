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


name="lumify-sphinxbase"
version="0.8"
release="dist"

_download \
    ${name} \
    http://downloads.sourceforge.net/project/cmusphinx/sphinxbase/${version}/sphinxbase-${version}.tar.gz \
    sphinxbase-${version}.tar.gz

if [ -d ${SOURCE_DIR}/sphinxbase-${version} ]; then
  mv ${SOURCE_DIR}/sphinxbase-${version} ${SOURCE_DIR}/${name}
fi

_build ${name} ${version} ${release}
