#!/bin/bash

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

[ "$1" ] && FILTER="$1" || FILTER='.'

(
  cd ${DIR}/..

  PLUGINS=$(find lumify-public/web/plugins lumify-public/core/plugins -mindepth 1 -maxdepth 1 -type d | grep ${FILTER})
  MODULES=$(echo ${PLUGINS} | sed -e 's/ /,/g')

  set -x
  mvn package -pl ${MODULES} -am -DskipTests -Dsource.skip=true
  set +x
)
