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
su - vagrant -c 'cd /vagrant && mvn package -DskipTests' 2>&1 \
  | tee /vagrant/mvn.log \
  | grep '\[INFO\] Building'

# deploy the webapp
cp /vagrant/deployment/application.xml /opt/jetty/contexts
cp /vagrant/lumify-public/web/target/application-1.0-SNAPSHOT.war /opt/jetty/webapps/application.war

# deploy the open source topology
/opt/storm/bin/storm jar \
  /vagrant/lumify-public/storm-lumify/target/lumify-storm-1.0-SNAPSHOT-jar-with-dependencies.jar \
  com.altamiracorp.lumify.storm.StormRunner \
  --datadir=/lumify/data

# TODO: sample data
# TODO: reset data
# TODO: howto deploy twitter topology
