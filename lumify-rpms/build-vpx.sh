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


name="lumify-vpx"
version="1.2.0"
release="dist"

_clone ${name} http://git.chromium.org/webm/libvpx.git v${version}

cd ${SOURCE_DIR}/${name}
git apply ${DIR}/source/vpx.patch

_build ${name} ${version} ${release}
