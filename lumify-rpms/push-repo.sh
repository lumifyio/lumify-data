#!/bin/bash -eu

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

source ${DIR}/setenv.sh

rm -rf ${HOME}/repo/repodata
createrepo --baseurl=${LUMIFYREPO_URL} ${HOME}/repo

s3cmd sync --delete-removed ${HOME}/repo/ s3://bits.lumify.io/yum/
