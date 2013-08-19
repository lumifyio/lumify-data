#!/bin/bash

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

import_directory=${DIR}/../data/import
import_zipfile=""
minimum_n=0

while [ $# -gt 0 ]; do
  echo "$1" | grep -Eq '^[0-9]+$'
  if [ $? -eq 0 ]; then
    minimum_n="$1"
  else
    import_directory="$2"
    import_zipfile="$1"
  fi
  shift
done

for step in $(ls ${DIR}/[0-9][0-9][0-9]_*.sh | sort); do
  step_n=$(basename ${step} | awk -F _ '{print $1}')
  [ ${step_n} -ge ${minimum_n} ] || continue

  echo -n $'\n\e[01;35m'
  echo $(basename ${step})
  echo -n $'\e[00;35m'
  printf '%*s\n' "${COLUMNS:-$(tput cols)}" | tr ' ' -
  echo -n $'\e[00;00m'

  echo "${step}" | grep -Eq '_FileImport.sh$'
  if [ $? -eq 0 ]; then
    time ${step} ${import_zipfile} ${import_directory}
  else
    time ${step}
  fi
  [ $? -eq 0 ] || exit
done
