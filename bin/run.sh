#!/bin/bash

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

if [ "$1" != '' ]; then
  directory=${1}
else
  directory=${DIR}/../data/import
fi

for step in $(ls ${DIR}/[0-9][0-9][0-9]_*.sh | sort); do
  echo -n $'\n\e[01;35m'
  echo $(basename ${step})
  echo -n $'\e[00;35m'
  printf '%*s\n' "${COLUMNS:-$(tput cols)}" | tr ' ' -
  echo -n $'\e[00;00m'

  if [ "${step}" == "${DIR}/100_FileImport.sh" ]; then
    time ${step} $directory
  else
    time ${step}
  fi
  [ $? -eq 0 ] || exit
done
