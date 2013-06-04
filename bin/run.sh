#!/bin/bash

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

for step in $(ls ${DIR}/[0-9][0-9][0-9]_*.sh | sort); do
  echo -n $'\e[01;35m'
  echo $(basename ${step})
  echo -n $'\e[00;35m'
  printf '%*s\n' "${COLUMNS:-$(tput cols)}" | tr ' ' -
  echo -n $'\e[00;00m'

  ${step}
  [ $? -eq 0 ] || exit
done
