#!/bin/bash

SSH_OPTS='-o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o LogLevel=QUIET'

hosts_file=$1

stormmaster=$(awk '/ +stormmaster/ {print $1}' ${hosts_file})

ssh ${SSH_OPTS} ${stormmaster} initctl start storm-nimbus
ssh ${SSH_OPTS} ${stormmaster} initctl start storm-ui

for node in $(awk '/node[0-9]+/ {print $1}' ${hosts_file}); do
  ssh ${SSH_OPTS} ${node} initctl start storm-supervisor
done
