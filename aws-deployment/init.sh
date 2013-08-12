#!/bin/bash -eu

PUPPETLABS_RPM_URL=http://yum.puppetlabs.com/el/6/products/i386/puppetlabs-release-6-7.noarch.rpm
SSH_OPTS='-o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o LogLevel=QUIET'

hosts_file=$1

grep -q $(hostname) /etc/hosts || cat ${hosts_file} >> /etc/hosts
sudo rpm -ivh ${PUPPETLABS_RPM_URL}
sudo yum -y install puppet-server

for other_host in $(awk -v localhost=$(hostname) '$2!=localhost {print $1}' ${hosts_file}); do
  scp ${SSH_OPTS} ${hosts_file} ${other_host}:
  ssh ${SSH_OPTS} -t ${other_host} sudo -s "grep -q ${other_host} /etc/hosts || cat ${hosts_file} >> /etc/hosts"
  echo ssh ${SSH_OPTS} -t ${other_host} sudo rpm -ivh ${PUPPETLABS_RPM_URL}
  echo ssh ${SSH_OPTS} -t ${other_host} sudo yum -y install puppet
  echo ssh ${SSH_OPTS} -t ${other_host} sudo puppet agent -t
done
