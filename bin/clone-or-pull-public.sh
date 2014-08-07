#!/bin/bash

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

GIT_URL=git@github.com:lumifyio/lumify.git
HTTPS_URL=https://github.com/lumifyio/lumify.git

cd ${DIR}/..

if [ -d "lumify-public" ]; then
    cd lumify-public
    git pull
else
    echo Cloning ${GIT_URL}
    git clone ${GIT_URL} lumify-public
    if [ $? -ne 0 ]; then
        echo Failed to clone using git url, falling back to ${HTTPS_URL}
        git clone ${HTTPS_URL} lumify-public
    fi
fi
