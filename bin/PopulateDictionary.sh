#!/bin/bash

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

classpath=$(${DIR}/classpath.sh dictionary-seed)
if [ $? -ne 0 ]; then
  echo "${classpath}"
  exit
fi

if [ "$1" == "--help" ]
then
  java \
  -Dfile.encoding=UTF-8 \
  -classpath ${classpath} \
  com.altamiracorp.reddawn.dictionary.DictionarySeederDriver \
  --help
  exit
fi

DIRECTORY=${DIR}/../dictionary-files/

while [ $# -ne 0 ]
do
    case "$1" in
        --types=*)
            TYPES=`cut -d "=" -f 2 <<< "$1"` ;;
        --directory=*)
            DIRECTORY=${DIR}/`cut -d "=" -f 2 <<< "$1"` ;;
    esac
    shift
done

declare -a PARAMS=("--directory=${DIRECTORY}")
if [ -n "$TYPES" ]
then
    PARAMS=("${PARAMS[@]}" "--types=${TYPES}")
fi

java \
-Dfile.encoding=UTF-8 \
-classpath ${classpath} \
com.altamiracorp.reddawn.dictionary.DictionarySeederDriver \
"${PARAMS[@]}"
