#!/bin/bash

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

(
  cd ${DIR}/..

  modules=$(find lumify-enterprise -name '*PropertyWorker.java' | cut -d / -f 1-2 | sort -u)
  modules_comma_separated=$(echo ${modules} | sed -e 's/ /,/g')

  set -x
  mvn install -pl lumify-root -am -DskipTests -Dsource.skip=true
  mvn package -pl ${modules_comma_separated} -am -DskipTests -Dsource.skip=true
  set +x

  for module in ${modules}; do
    simple_module_name=$(echo ${module} | cut -d / -f 2)
    if [ -f ${module}/target/${simple_module_name}-*-jar-with-dependencies.jar ]; then
      ls ${module}/target/${simple_module_name}-*-jar-with-dependencies.jar
    else
      echo "WARNING: no uber-jar for ${module}"
    fi
  done
)
