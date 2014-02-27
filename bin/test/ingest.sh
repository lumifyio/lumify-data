#!/bin/bash -e

clone_dir=$1

cd ${clone_dir} && mvn compile -DskipTests 2>&1 | tee /vagrant/mvn.out

/opt/lumify/format.sh

curl http://s3.amazonaws.com/RedDawn/DataSets/Testing/data.tar | tar -C ${clone_dir}/data -xf -

echo 'running bin/080_Ontology.sh'
cd ${clone_dir} && bin/080_Ontology.sh &> /vagrant/080_Ontology.out

echo 'running bin/stormLocal.sh in the background'
cd ${clone_dir} && bin/stormLocal.sh &> /vagrant/stormLocal.out &
echo $! > /vagrant/stormLocal.pid

echo 'running bin/stormEnterpriseLocal.sh in the background'
cd ${clone_dir} && bin/stormEnterpriseLocal.sh &> /vagrant/stormEnterpriseLocal.out &
echo $! > /vagrant/stormEnterpriseLocal.pid

echo 'running bin/900_Server.sh in the background'
cd ${clone_dir} && bin/900_Server.sh &> /vagrant/900_Server.out &
echo $! > /vagrant/900_Server.pid

echo 'sleeping for 5 minutes...'
sleep 300
