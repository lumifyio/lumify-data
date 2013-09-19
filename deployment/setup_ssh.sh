#!/bin/bash -eu

SSH_OPTS='-o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o LogLevel=QUIET'

hosts_file=$1

namenode=$(awk '/ +namenode/ {print $1}' ${hosts_file})
accumulomaster=$(awk '/ +accumulomaster/ {print $1}' ${hosts_file})

for node in $(awk '/node[0-9]+/ {print $1}' ${hosts_file}); do
  ssh ${SSH_OPTS} ${namenode}       'cat ~hdfs/.ssh/id_rsa.pub'     | ssh ${SSH_OPTS} ${node} 'cat >> ~hdfs/.ssh/authorized_keys'
  ssh ${SSH_OPTS} ${accumulomaster} 'cat ~accumulo/.ssh/id_rsa.pub' | ssh ${SSH_OPTS} ${node} 'cat >> ~accumulo/.ssh/authorized_keys'
done
