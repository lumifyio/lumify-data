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

  set -x
  mvn package -P web-war -pl lumify-public/web/war -am -DskipTests -Dsource.skip=true
  set +x

  if [ -f lumify-public/web/war/target/lumify-web-war-*.war ]; then
    webapps_dir=$(find /opt -maxdepth 2 -type d -name webapps)
    set -x
    sudo rm -rf ${webapps_dir}/lumify*
    sudo cp lumify-public/web/war/target/lumify-web-war-*.war ${webapps_dir}/lumify.war
    set +x
  fi
)
