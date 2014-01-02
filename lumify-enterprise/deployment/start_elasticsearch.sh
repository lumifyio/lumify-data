#!/bin/bash

SSH_OPTS='-o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o LogLevel=QUIET'

hosts_file=$1

for node in $(awk '/node[0-9]+/ {print $1}' ${hosts_file}); do
  ssh ${SSH_OPTS} ${node} initctl start elasticsearch
done
