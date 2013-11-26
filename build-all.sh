#!/bin/bash -e

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

source ${DIR}/setenv.sh


${DIR}/build-x264.sh
${DIR}/build-fdk-aac.sh
${DIR}/build-lame.sh
${DIR}/build-opus.sh
${DIR}/build-ogg.sh

# sudo rpm -i ${LUMIFYREPO_DIR}/RPMS/x86_64/lumify-ogg-*.rpm
${DIR}/build-vorbis.sh

${DIR}/build-vpx.sh
${DIR}/build-theora.sh

# for rpm in ${LUMIFYREPO_DIR}/RPMS/x86_64/*.rpm; do
#  sudo rpm -i ${rpm}
# done
${DIR}/build-ffmpeg.sh

${DIR}/build-ccextractor.sh
