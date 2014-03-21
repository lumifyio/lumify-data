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


name="lumify-pocketsphinx"
version="0.8"
release="dist"

_download \
    ${name} \
    http://downloads.sourceforge.net/project/cmusphinx/pocketsphinx/${version}/pocketsphinx-${version}.tar.gz \
    pocketsphinx-${version}.tar.gz

if [ -d ${SOURCE_DIR}/pocketsphinx-${version} ]; then
  mv ${SOURCE_DIR}/pocketsphinx-${version} ${SOURCE_DIR}/${name}
fi

_build ${name} ${version} ${release}
