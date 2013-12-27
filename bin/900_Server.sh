#!/bin/bash

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

pushd ${DIR}/../lumify-public/lumify-web > /dev/null
MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=512m"
mvn \
	-Dfile.encoding=UTF-8 \
	-Djava.awt.headless=true \
	-Djava.security.krb5.realm= \
	-Djava.security.krb5.kdc= \
	-Djetty.keystore.path=${DIR}/../config/ssl/lumify.jks \
	-Djetty.keystore.password=password \
	$LUMIFY_JETTY_ARGS \
	jetty:run
popd > /dev/null
