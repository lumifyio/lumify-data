#!/bin/bash -e

tgz_file=$1

cd /vagrant && mvn package -P uber-jar,web-war -DskipTests 2>&1 | tee /vagrant/mvn.out

/opt/lumify/format.sh

sudo sh -c "echo 'objectdetection.opencv.disabled=true' > /opt/lumify/config/z-disable-opencv.properties"
sudo sh -c "echo 'clavin.disabled=true' > /opt/lumify/config/z-disable-clavin.properties"

cat /vagrant/${tgz_file} | tar -C /vagrant/data -xzf -

echo 'running bin/080_Ontology.sh'
cd /vagrant && bin/080_Ontology.sh &> /vagrant/080_Ontology.out

echo 'running bin/stormEnterpriseLocal.sh in the background'
cd /vagrant && bin/stormEnterpriseLocal.sh &> /vagrant/stormEnterpriseLocal.out &
echo $! > /vagrant/stormEnterpriseLocal.pid

echo 'deploy the .war'
sudo cp /vagrant/deployment/lumify.xml /opt/jetty/contexts
sudo cp /vagrant/lumify-public/lumify-web-war/target/lumify-web-war-*.war /opt/jetty/webapps/lumify.war
sudo service jetty restart

echo 'sleeping for 10 minutes...'
for n in {9..0}; do
  sleep 60
  echo ${n}
done
