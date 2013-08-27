#!/bin/bash

SSH_OPTS='-o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o LogLevel=QUIET'

hosts_file=$1

blurcontroller=$(awk '/ +blurcontroller/ {print $1}' ${hosts_file})

ssh ${SSH_OPTS} ${blurcontroller} su - blur -c /usr/lib/apache-blur/bin/start-controller-server.sh

for node in $(awk '/node[0-9]+/ {print $1}' ${hosts_file}); do
  ssh ${SSH_OPTS} ${node} su - blur -c /usr/lib/apache-blur/bin/start-shard-server.sh
done
