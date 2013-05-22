#!/bin/bash

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

dir=$1

if [ -d ${DIR}/../${dir} ]; then
  mvn_output=$(cd ${DIR}/.. && mvn clean package)
  mvn_exit=$?
  if [ ${mvn_exit} -ne 0 ]; then
    echo ${mvn_output}
    exit ${mvn_exit}
  fi

  if [ -f ${DIR}/../${dir}/target/.classpath ]; then
    if [ -d ${DIR}/../${dir}/target/classes ]; then
      echo "${DIR}/../${dir}/target/classes:$(cat ${DIR}/../${dir}/target/.classpath)"
    else
      echo "${dir}/target/classes not found"
      exit 3
    fi
  else
    echo "${dir}/target/.classpath not found"
    exit 2
  fi
else
  echo "${dir} is not a valid directory"
  exit 1
fi
