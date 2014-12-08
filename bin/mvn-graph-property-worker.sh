#!/bin/bash

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

if [ "$1" ]; then
  FILTER="$1"
else
  FILTER='.'
  OTHER_MODULES='
    lumify-public/graph-property-worker/graph-property-worker-yarn
  '
fi

(
  cd ${DIR}/..

  GPW_MODULES=$(find lumify-public/graph-property-worker/plugins -mindepth 1 -maxdepth 1 -type d ! -name target  | grep ${FILTER})
  MODULES=$(echo ${GPW_MODULES} ${OTHER_MODULES} | sed -e 's/ /,/g')

  set -x
  mvn package -pl ${MODULES} -am -DskipTests -Dsource.skip=true
  set +x

  for module in $(echo ${MODULES} | sed -e 's/,/ /g'); do
    if [ -f ${module}/target/*-jar-with-dependencies.jar ]; then
      file=$(ls ${module}/target/*-jar-with-dependencies.jar)
      files="${files} ${DIR}/../${file}"
    else
      warnings="${warnings} ${module}"
    fi
  done

  if [ "${warnings}" != '' ]; then
    echo -n 'WARNING: the following module(s) did not create an uber-jar:' >&2
    echo "${warnings}" | sed -e 's/ /\n  /g' >&2
  fi

  echo -n 'hadoop fs -put'
  echo -n "${files}" | sed -e 's/ / \\\n  /g'
  echo ' \'
  echo '  /lumify/libcache'
)
