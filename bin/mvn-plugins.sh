#!/bin/bash

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

MODULES="
  lumify-public/lumify-web-auth-oauth
  lumify-public/lumify-web-auth-username-only
  lumify-public/lumify-web-dev-tools
  lumify-public/lumify-web-import-export-workspaces
  lumify-enterprise/lumify-opennlp-dictionary-extractor
"
MODULES=$(echo ${MODULES} | sed -e 's/ /,/g')

(
  cd ${DIR}/..

  set -x
  mvn install -pl lumify-root -am -DskipTests -Dsource.skip=true
  mvn package -pl ${MODULES} -am -DskipTests -Dsource.skip=true
  set +x
)
