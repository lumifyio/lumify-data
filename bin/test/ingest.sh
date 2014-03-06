#!/bin/bash -e

clone_dir=$1

for pid_file in $(ls /vagrant/*.pid); do
  name=$(basename ${pid_file} .pid)
  echo "checking for running ${name} processes..."
  pid=$(pgrep ${name} || true)
  if [ "${pid}" = "$(cat ${pid_file})" ]; then
    echo "killing ${pid} and children..."
    pkill -9 -P ${pid} || true
  fi
  rm -f ${pid_file}
done

sudo yum install -y lsof
for port in 8080 8443; do
  echo "checking for running processes listening on port ${port}..."
  pid=$(lsof -i TCP:${port} -s TCP:LISTEN -t || true)
  if [ "${pid}" ]; then
    echo "killing:"
    lsof -i TCP:${port} -s TCP:LISTEN -P
    kill -9 ${pid} || true
  fi
done

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
for n in {4..0}; do
  sleep 60
  echo ${n}
done
