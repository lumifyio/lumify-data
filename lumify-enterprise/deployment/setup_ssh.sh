#!/bin/bash -eu

SSH_OPTS='-o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o LogLevel=QUIET'

hosts_file=$1

function _create_keypair {
  local host=$1
  local user=$2

  ssh ${SSH_OPTS} ${host} "[ -f ~${user}/.ssh/id_rsa ] || su - ${user} -c 'ssh-keygen -q -N \"\" -f ~${user}/.ssh/id_rsa'"
}

function _add_authorized_key {
   local src_host=$1
   local dst_host=$2
   local user=$3
   local group=$4

   ssh ${SSH_OPTS} ${dst_host} "mkdir -p ~${user}/.ssh && chown ${user}:${group} ~${user}/.ssh && chmod u=rwx,go= ~${user}/.ssh"
   ssh ${SSH_OPTS} ${src_host}  "cat ~${user}/.ssh/id_rsa.pub" | ssh ${SSH_OPTS} ${dst_host} "cat >> ~${user}/.ssh/authorized_keys"
}

namenode=$(awk '/ +namenode/ {print $1}' ${hosts_file})
accumulomaster=$(awk '/ +accumulomaster/ {print $1}' ${hosts_file})

_create_keypair ${namenode} hdfs
_create_keypair ${accumulomaster} accumulo

for node in $(awk '/node[0-9]+/ {print $1}' ${hosts_file}); do
  _add_authorized_key ${namenode} ${node} hdfs hadoop
  _add_authorized_key ${accumulomaster} ${node} accumulo hadoop
done
