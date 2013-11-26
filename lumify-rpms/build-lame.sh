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

name="lumify-lame"
version="3.99.5"
release="dist"

_download \
    ${name} \
    http://downloads.sourceforge.net/project/lame/lame/$(echo ${version} | sed -e 's/\.[0-9]*$//')/lame-${version}.tar.gz \
    lame-${version}.tar.gz

if [ -d ${SOURCE_DIR}/lame-${version} ]; then
  mv ${SOURCE_DIR}/lame-${version} ${SOURCE_DIR}/${name}
fi

_build ${name} ${version} ${release}
