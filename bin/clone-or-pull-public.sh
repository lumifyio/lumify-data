#!/bin/bash

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

cd ${DIR}/..

if [ -d "lumify-public" ]; then
    cd lumify-public
    git pull
else
    git clone git@github.com:lumifyio/lumify.git lumify-public
    if [ $? -ne 0 ]; then
        git clone https://github.com/lumifyio/lumify.git lumify-public
    fi
fi
