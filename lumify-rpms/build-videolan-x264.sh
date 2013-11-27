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


name="lumify-videolan-x264"

_clone ${name} http://git.videolan.org/git/x264.git stable

cd ${SOURCE_DIR}/${name}
version=$(./version.sh | awk -F '"' '/X264_POINTVER/ {print $2}' | cut -d ' ' -f 1)
release=$(./version.sh | awk -F '"' '/X264_POINTVER/ {print $2}' | cut -d ' ' -f 2)

_build ${name} ${version} ${release}
