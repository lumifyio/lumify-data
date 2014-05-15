#!/bin/bash

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

AUTH_PLUGINS=$(find ${DIR}/.. -type d -name 'lumify-web-auth-*' | sed -e "s|${DIR}/../||")
OTHER_PLUGINS="
  lumify-public/lumify-web-dev-tools
  lumify-public/lumify-web-import-export-workspaces
  lumify-enterprise/lumify-opennlp-dictionary-extractor-web
"
MODULES=$(echo ${AUTH_PLUGINS} ${OTHER_PLUGINS} | sed -e 's/ /,/g')

(
  cd ${DIR}/..

  set -x
  mvn install -pl lumify-root -am -DskipTests -Dsource.skip=true
  mvn package -pl ${MODULES} -am -DskipTests -Dsource.skip=true
  set +x
)
