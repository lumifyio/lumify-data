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

name="lumify-kafka"
version="0.8.0"
release="dist"

_download \
    ${name} \
    http://archive.apache.org/dist/kafka/${version}/kafka-${version}-src.tgz \
    kafka-${version}.tar.gz

if [ -d ${SOURCE_DIR}/kafka-${version}-src ]; then
  mv ${SOURCE_DIR}/kafka-${version}-src ${SOURCE_DIR}/${name}
fi

_build ${name} ${version} ${release}
