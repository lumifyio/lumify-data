#!/bin/bash -e

if [ "$1" ]; then
  branch_name=$(echo $1 | sed -e 's|origin/||')
  cd /vagrent && git checkout ${branch_name}
fi

rm -rf /tmp/lumify-all
git clone file:///vagrant /tmp/lumify-all --depth 1

cd /tmp/lumify-all && mvn compile -DskipTests 2>&1 | tee /vagrant/mvn.out

/opt/lumify/format.sh

curl http://s3.amazonaws.com/RedDawn/DataSets/Testing/data.tar | tar -C /tmp/lumify-all/data -xf -

echo 'running bin/080_Ontology.sh'
cd /tmp/lumify-all && bin/080_Ontology.sh &> /vagrant/080_Ontology.out

echo 'running bin/stormLocal.sh in the background'
cd /tmp/lumify-all && bin/stormLocal.sh &> /vagrant/stormLocal.out &
echo $! > /vagrant/stormLocal.pid

echo 'running bin/stormEnterpriseLocal.sh in the background'
cd /tmp/lumify-all && bin/stormEnterpriseLocal.sh &> /vagrant/stormEnterpriseLocal.out &
echo $! > /vagrant/stormEnterpriseLocal.pid

echo 'running bin/900_Server.sh in the background'
cd /tmp/lumify-all && bin/900_Server.sh &> /vagrant/900_Server.out &
echo $! > /vagrant/900_Server.pid

echo 'sleeping for 5 minutes...'
sleep 60
echo 'sleeping for 4 minutes...'
sleep 60
echo 'sleeping for 3 minutes...'
sleep 60
echo 'sleeping for 2 minutes...'
sleep 60
echo 'sleeping for 1 minute...'
sleep 60
