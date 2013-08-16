#!/bin/bash -eu

PUPPETLABS_RPM_URL='http://yum.puppetlabs.com/el/6/products/i386/puppetlabs-release-6-7.noarch.rpm'
SSH_OPTS='-o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o LogLevel=QUIET'

hosts_file=$1


# configure enviornment name resolution
grep -q $(hostname) /etc/hosts || cat ${hosts_file} >> /etc/hosts

# add the PuppetLabs Yum repo, install and enable puppet
rpm -ivh ${PUPPETLABS_RPM_URL}
yum -y install puppet-server
chkconfig puppetmaster on
chkconfig puppet on

# setup SSL certs
puppet cert generate $(hostname) --dns_alt_names=puppet
awk -v localhost=$(hostname) '$2!=localhost {print $2}' ${hosts_file} > /etc/puppet/autosign.conf

# insert a firewall rule for the puppermaster and tinyproxy services
rule_number=$(iptables -L -n --line-numbers | awk '/tcp dpt:22/ {print $1}')
iptables -I INPUT ${rule_number} -p tcp -m state --state NEW -m tcp --dport 8140 -j ACCEPT
rule_number=$(iptables -L -n --line-numbers | awk '/tcp dpt:22/ {print $1}')
iptables -I INPUT ${rule_number} -p tcp -m state --state NEW -m tcp --dport 8080 -j ACCEPT
service iptables save

cat >> /etc/puppet/puppet.conf <<EO_PUPPET_CONF

[master]
    modulepath = \$confdir/modules:/usr/share/puppet/modules:\$confdir/reddawn-modules:\$confdir/puppet-modules
EO_PUPPET_CONF

./update.sh start

# run puppet once in the forground
puppet agent -t

# run puppet as a service
service puppet start


for other_host in $(awk -v localhost=$(hostname) '$2!=localhost {print $1}' ${hosts_file}); do
  # configure enviornment name resolution
  scp ${SSH_OPTS} ${hosts_file} ${other_host}:
  ssh ${SSH_OPTS} ${other_host} "grep -q ${other_host} /etc/hosts || cat ${hosts_file} >> /etc/hosts"

  # configure Yum to use the proxy
  cat <<EO_YUM_CONF | ssh ${SSH_OPTS} ${other_host} 'cat >> /etc/yum.conf'

  proxy=http://$(hostname):8080
EO_YUM_CONF

  # add the PuppetLabs Yum repo, install and enable puppet
  ssh ${SSH_OPTS} ${other_host} http_proxy=http://$(hostname):8080 rpm -ivh ${PUPPETLABS_RPM_URL}
  ssh ${SSH_OPTS} ${other_host} yum -y install puppet
  ssh ${SSH_OPTS} ${other_host} chkconfig puppet on

  # run puppet once in the forground
  ssh ${SSH_OPTS} ${other_host} puppet agent -t

  # run puppet as a service
  ssh ${SSH_OPTS} ${other_host} service puppet start
done
