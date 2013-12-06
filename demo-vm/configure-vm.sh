#!/bin/bash

LUMIFY_USERNAME=lumify
LUMIFY_PASSWORD=lumify

id -u ${LUMIFY_USERNAME} > /dev/null \
    || useradd ${LUMIFY_USERNAME}

id -Gn ${LUMIFY_USERNAME} | grep -q wheel \
    || usermod -a -G wheel ${LUMIFY_USERNAME}

echo "${LUMIFY_PASSWORD}
${LUMIFY_PASSWORD}" | passwd ${LUMIFY_USERNAME} 2> /dev/null

cat <<-EOM > /etc/sudoers.d/${LUMIFY_USERNAME}
${LUMIFY_USERNAME} ALL=(ALL) NOPASSWD: ALL
EOM

cat <<-EOM > /etc/motd

Welcome to Lumify
=================
http://lumify.io

EOM

mkdir -p /opt/lumify/config
cp /vagrant/demo-vm/configuration.properties /opt/lumify/config/configuration.properties

# run maven
su - vagrant -c 'cd /vagrant && mvn package install -DskipTests' 2>&1 \
  | tee /vagrant/mvn.log \
  | grep '\[INFO\] Building'

# deploy the webapp
cp /vagrant/deployment/application.xml /opt/jetty/contexts
cp /vagrant/lumify-public/web/target/application-1.0-SNAPSHOT.war /opt/jetty/webapps/application.war

# deploy the open source topology
/opt/storm/bin/storm list \
  | grep -q 'lumify\s*ACTIVE' && /opt/storm/bin/storm kill lumify
/opt/storm/bin/storm jar \
  /vagrant/lumify-public/storm-lumify/target/lumify-storm-1.0-SNAPSHOT-jar-with-dependencies.jar \
  com.altamiracorp.lumify.storm.StormRunner

# insert sample data
cp /vagrant/bin/accumulo-import.sh /opt/lumify
cp /vagrant/bin/rebuild-index.sh /opt/lumify
cp /vagrant/demo-vm/sample-data.tgz /opt/lumify
mkdir -p /opt/lumify/lib
cp /vagrant/lumify-public/storm-lumify/target/lumify-storm-*-with-dependencies.jar /opt/lumify/lib
/opt/lumify/format.sh
/opt/lumify/accumulo-import.sh /opt/lumify/sample-data.tgz
/opt/lumify/rebuild-index.sh
