#!/bin/bash -e

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

source ${DIR}/setenv.sh

function _build_and_install {
  local name=$1

  ${DIR}/build-${name}.sh
  sudo rpm -i ${LUMIFYREPO_DIR}/RPMS/x86_64/lumify-${name}-*.rpm
}

_build_and_install videolan-x264
_build_and_install fdk-aac
_build_and_install lame
_build_and_install opus
_build_and_install ogg
_build_and_install vorbis
_build_and_install vpx
_build_and_install theora
_build_and_install ffmpeg

_build_and_install ccextractor

_build_and_install opencv

_build_and_install leptonica
_build_and_install tesseract
_build_and_install tesseract-eng
