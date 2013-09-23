#!/bin/bash

SSH_OPTS='-o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o LogLevel=QUIET'

hosts_file=$1

for zk in $(awk '/zk[0-9]+/ {print $1}' ${hosts_file}); do
  ssh ${SSH_OPTS} ${zk} service hadoop-zookeeper-server start
done
