#!/bin/bash

SSH_OPTS='-o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o LogLevel=QUIET'

hosts_file=$1

namenode=$(awk '/ +namenode/ {print $1}' ${hosts_file})
secondarynamenode=$(awk '/ +secondarynamenode/ {print $1}' ${hosts_file})

echo "ssh to ${namenode} and as the hdfs user run: hadoop namenode -format"
while [ "${ready}" != 'yes' ]; do
  echo "then type 'yes' and press return"
  read ready
done

ssh ${SSH_OPTS} ${namenode} service hadoop-0.20-namenode start
ssh ${SSH_OPTS} ${secondarynamenode} service hadoop-0.20-secondarynamenode start

for node in $(awk '/node[0-9]+/ {print $1}' ${hosts_file}); do
  for n in 1 2 3; do
    ssh ${SSH_OPTS} ${node} mkdir -p /data${n}/hadoop/tmp
    ssh ${SSH_OPTS} ${node} chown -R hdfs:hadoop /data${n}/hadoop
    ssh ${SSH_OPTS} ${node} mkdir -p /data${n}/hdfs/data /data${n}/hdfs/name
    ssh ${SSH_OPTS} ${node} chown -R hdfs:hadoop /data${n}/hdfs
    ssh ${SSH_OPTS} ${node} mkdir -p /data${n}/mapred/local
    ssh ${SSH_OPTS} ${node} chown -R mapred:hadoop /data${n}/mapred
  done
  ssh ${SSH_OPTS} ${node} service hadoop-0.20-datanode start
  ssh ${SSH_OPTS} ${node} service hadoop-0.20-tasktracker start
done

ssh ${SSH_OPTS} ${namenode} service hadoop-0.20-jobtracker start
