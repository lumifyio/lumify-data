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
    for webapp_dir in $(find /opt -maxdepth 2 -type d -name webapps); do
      set -x
      sudo rm -rf ${webapp_dir}/lumify*
      sudo cp lumify-public/web/war/target/lumify-web-war-*.war ${webapp_dir}/lumify.war
      set +x
    done
  fi
)
