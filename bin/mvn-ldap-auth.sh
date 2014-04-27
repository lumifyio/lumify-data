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
  mvn install -pl lumify-root -am -DskipTests -Dsource.skip=true
  mvn package -pl lumify-ldap-auth -am -DskipTests -Dsource.skip=true
  set +x

  if [ -f lumify-ldap-auth/target/lumify-ldap-auth-*-jar-with-dependencies.jar ]; then
    set -x
    sudo rm -f /opt/lumify/lib/lumify-ldap-auth-*-jar-with-dependencies.jar
    sudo cp lumify-ldap-auth/target/lumify-ldap-auth-*-jar-with-dependencies.jar /opt/lumify/lib
    set +x
  fi
)
