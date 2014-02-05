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

cat <<-EOM | tee /etc/motd | tee /etc/issue > /etc/issue.net

Welcome to the Lumify Demonstration VM
======================================
For more information about Lumify, please visit http://lumify.io
Built on $(date +'%Y-%m-%d')

EOM

if [ "$2" == '' ]; then
  echo "Must provide name of tar file for sample data" 
  exit 1
fi


cd /vagrant && bin/config.sh enterprise

if [ "$1" == 'opensource' ]; then
  cp /vagrant/demo-vm/demo-opensource.properties /opt/lumify/config/
fi

su - vagrant -c '/opt/lumify/format.sh'

# run maven
su - vagrant -c 'cd /vagrant && cd lumify-root && mvn clean install && cd .. &&  mvn package -P storm-jar,web-war -DskipTests' 2>&1 \
  | tee /vagrant/mvn.log \
  | grep '\[INFO\] Building'

# deploy the webapp
cp /vagrant/deployment/lumify.xml /opt/jetty/contexts
cp /vagrant/lumify-public/lumify-web-war/target/lumify-web-war-1.0-SNAPSHOT.war /opt/jetty/webapps/lumify.war

# load the ontology
su - vagrant -c 'cd /vagrant && bin/080_Ontology.sh'

# ingest sample data
cp /vagrant/demo-vm/$2 /opt/lumify
tar xvf /opt/lumify/$2 -C /opt/lumify
sudo -u hdfs hadoop fs -mkdir /lumify/data/unknown/
sudo -u hdfs hadoop fs -put /opt/lumify/import/* /lumify/data/unknown/
rm -rf /opt/lumify/import

# deploy the open source topology
/opt/storm/bin/storm list \
  | grep -q 'lumify\s*ACTIVE' && /opt/storm/bin/storm kill -w 1 lumify && /opt/storm/bin/storm kill -w 1 lumify-enterprise
/opt/storm/bin/storm jar \
 /vagrant/lumify-public/lumify-storm/target/lumify-storm-1.0-SNAPSHOT-jar-with-dependencies.jar \
 com.altamiracorp.lumify.storm.StormRunner
/opt/storm/bin/storm jar \
 /vagrant/lumify-enterprise/lumify-enterprise-storm/target/lumify-enterprise-storm-1.0-SNAPSHOT-jar-with-dependencies.jar \
 com.altamiracorp.lumify.storm.StormEnterpriseRunner 
