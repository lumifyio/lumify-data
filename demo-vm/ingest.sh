#!/bin/bash -e

tgz_file=$1

echo 'building war...'
/vagrant/bin/mvn-war.sh 2>&1 | tee /vagrant/mvn-war.out

echo 'building authentication plugin...'
/vagrant/bin/mvn-plugins.sh username-only 2>&1 | tee /vagrant/mvn-plugins.out

echo 'formatting...'
/opt/lumify/format.sh 2>&1 | tee /vagrant/format.out

echo 'importing data...'
cat /vagrant/${tgz_file} | tar -C /vagrant/data -xzf - 2>&1 | tee /vagrant/importData.out
/vagrant/lumify-public/bin/importData.sh /vagrant/data/import 2>&1 | tee -a /vagrant/importData.out

echo 'loading ontology...'
/vagrant/lumify-public/bin/owlImport.sh -i /vagrant/lumify-public/examples/ontology-dev/dev.owl --iri http://lumify.io/dev 2>&1 | tee /vagrant/owlImport.out

echo 'restarting jetty...'
sudo cp /vagrant/deployment/lumify.xml /opt/jetty/contexts
sudo cp /vagrant/lumify-public/web/war/target/lumify-web-war-*.war /opt/jetty/webapps/lumify.war
sudo mkdir -p /opt/lumify/lib
sudo cp /vagrant/lumify-public/web/plugins/auth-username-only/target/lumify-web-auth-username-only-*.jar /opt/lumify/lib
sudo service jetty restart

echo 'sleeping for 10 minutes...'
for n in {9..0}; do
  sleep 60
  echo ${n}
done
