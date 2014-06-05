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

  PLUGINS=$(find . -type f -name io.lumify.web.WebAppPlugin \
              | grep /src \
              | grep ${FILTER} \
              | sed -e 's|./||' -e  's|/src.*||' \
              | sort)
  MODULES=$(echo ${PLUGINS} | sed -e 's/ /,/g')

  set -x
  mvn install -pl lumify-root -am -DskipTests -Dsource.skip=true
  mvn package -pl ${MODULES} -am -DskipTests -Dsource.skip=true
  set +x
)
