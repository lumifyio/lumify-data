#!/bin/bash -eu

PUPPETLABS_RPM_URL='http://yum.puppetlabs.com/el/6/products/i386/puppetlabs-release-6-7.noarch.rpm'
SSH_OPTS='-o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o LogLevel=QUIET'

hosts_file=$1


# configure enviornment name resolution
grep -q $(hostname) /etc/hosts || cat ${hosts_file} >> /etc/hosts

# add the PuppetLabs YUM repo, install and enable puppet
rpm -ivh ${PUPPETLABS_RPM_URL}
yum -y install puppet-server
chkconfig puppetmaster on
chkconfig puppet on

# setup SSL certs
puppet cert generate $(hostname) --dns_alt_names=puppet
awk -v localhost=$(hostname) '$2!=localhost {print $2}' ${hosts_file} > /etc/puppet/autosign.conf

# insert a firewall rule for the puppermaster service
rule_number=$(iptables -L -n --line-numbers | awk '/tcp dpt:22/ {print $1}')
iptables -I INPUT ${rule_number} -p tcp -m state --state NEW -m tcp --dport 8140 -j ACCEPT
service iptables save

latest_modules=$(ls ~/modules-*.tgz | tail -1)
latest_puppet_modules=$(ls ~/puppet-modules-*.tgz | tail -1)
( cd /etc/puppet
  tar xzf ${latest_modules}
  unlink reddawn-modules || true
  ln -s $(basename ${latest_modules} .tgz)/puppet/modules reddawn-modules
  unlink hiera || true
  ln -s $(basename ${latest_modules} .tgz)/puppet/hiera hiera
  unlink hiera.yaml || true
  ln -s $(basename ${latest_modules} .tgz)/puppet/hiera-reddawn_demo.yaml hiera.yaml
  tar xzf ${latest_puppet_modules}
  unlink puppet-modules || true
  ln -s $(basename ${latest_puppet_modules} .tgz) puppet-modules
)
( cd /etc/puppet/manifests
  unlink site.pp || true
  ln -s ../$(basename ${latest_modules} .tgz)/puppet/manifests/reddawn_demo.pp site.pp
)

cat >> /etc/puppet/puppet.conf <<EO_PUPPET_CONF

[master]
    modulepath = \$confdir/modules:/usr/share/puppet/modules:\$confdir/reddawn-modules:\$confdir/puppet-modules
EO_PUPPET_CONF

# start the puppetmaster service
service puppetmaster start

# run puppet once in the forground
puppet agent -t

# run puppet as a service
service puppet start


for other_host in $(awk -v localhost=$(hostname) '$2!=localhost {print $1}' ${hosts_file}); do
  # configure enviornment name resolution
  scp ${SSH_OPTS} ${hosts_file} ${other_host}:
  ssh ${SSH_OPTS} ${other_host} "grep -q ${other_host} /etc/hosts || cat ${hosts_file} >> /etc/hosts"

  # add the PuppetLabs YUM repo, install and enable puppet
  ssh ${SSH_OPTS} ${other_host} rpm -ivh ${PUPPETLABS_RPM_URL}
  ssh ${SSH_OPTS} ${other_host} yum -y install puppet
  ssh ${SSH_OPTS} ${other_host} chkconfig puppet on

  # run puppet once in the forground
  ssh ${SSH_OPTS} ${other_host} puppet agent -t

  # run puppet as a service
  ssh ${SSH_OPTS} ${other_host} service puppet start
done
