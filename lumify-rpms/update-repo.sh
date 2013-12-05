#!/bin/bash -eu

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

source ${DIR}/setenv.sh


s3cmd sync --exclude 'repodata/*' s3://bits.lumify.io/yum/ repo/

rm -rf repo/repodata
createrepo --baseurl=${LUMIFYREPO_URL} repo

s3cmd sync --delete-removed repo/ s3://bits.lumify.io/yum/
