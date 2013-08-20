#!/bin/bash -eu

PUPPETLABS_RPM_URL='http://yum.puppetlabs.com/el/6/products/i386/puppetlabs-release-6-7.noarch.rpm'
SSH_OPTS='-o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o LogLevel=QUIET'

hosts_file=$1

function heading {
  local text=$1

  echo -n $'\n\e[01;35m'
  echo ${text}
  echo -n $'\e[00;35m'
  printf '%*s\n' "${COLUMNS:-$(tput cols)}" | tr ' ' -
  echo -n $'\e[00;00m'
}


heading 'configure enviornment name resolution'
grep -q $(hostname) /etc/hosts || cat ${hosts_file} >> /etc/hosts

heading 'add the PuppetLabs yum repo, install and enable puppet'
rpm -ivh ${PUPPETLABS_RPM_URL}
yum -y install puppet-server
chkconfig puppetmaster on
chkconfig puppet on

heading 'setup SSL certs'
puppet cert generate $(hostname) --dns_alt_names=puppet
awk -v localhost=$(hostname) '$2!=localhost {print $2}' ${hosts_file} > /etc/puppet/autosign.conf

heading 'insert firewall rules for the puppermaster and tinyproxy services'
rule_number=$(iptables -L -n --line-numbers | awk '/tcp dpt:22/ {print $1}')
iptables -I INPUT ${rule_number} -p tcp -m state --state NEW -m tcp --dport 8140 -j ACCEPT
rule_number=$(iptables -L -n --line-numbers | awk '/tcp dpt:22/ {print $1}')
iptables -I INPUT ${rule_number} -p tcp -m state --state NEW -m tcp --dport 8080 -j ACCEPT
service iptables save

# TODO: firewall rules for the cluster services

heading 'configure puppet'
cat >> /etc/puppet/puppet.conf <<EO_PUPPET_CONF

[master]
    modulepath = \$confdir/modules:/usr/share/puppet/modules:\$confdir/reddawn-modules:\$confdir/puppet-modules
EO_PUPPET_CONF

heading 'install PuppetLabs modules'
puppet module install puppetlabs/firewall

heading 'install our configuration and modules, start the puppetmaster service'
./update.sh start

heading 'run puppet once in the forground'
puppet agent -t || true

heading 'run puppet as a service'
service puppet start


for other_host in $(awk -v localhost=$(hostname) '$2!=localhost {print $1}' ${hosts_file}); do
  heading "${other_host}: configure enviornment name resolution"
  scp ${SSH_OPTS} ${hosts_file} ${other_host}:
  ssh ${SSH_OPTS} ${other_host} "grep -q ${other_host} /etc/hosts || cat ${hosts_file} >> /etc/hosts"

  heading "${other_host}: disable IPv6"
  ssh ${SSH_OPTS} ${other_host} sysctl -w net.ipv6.conf.all.disable_ipv6=1
  cat <<EO_SYSCTL_CONF | ssh ${SSH_OPTS} ${other_host} 'cat >> /etc/sysctl.conf'

net.ipv6.conf.all.disable_ipv6 = 1
EO_SYSCTL_CONF

  heading "${other_host}: configure yum to use the proxy"
  cat <<EO_YUM_CONF | ssh ${SSH_OPTS} ${other_host} 'cat >> /etc/yum.conf'

proxy=http://$(hostname):8080
EO_YUM_CONF

  heading "${other_host}: add the PuppetLabs yum repo, install and enable puppet"
  ssh ${SSH_OPTS} ${other_host} http_proxy=http://$(hostname):8080 rpm -ivh ${PUPPETLABS_RPM_URL}
  ssh ${SSH_OPTS} ${other_host} yum -y install puppet
  ssh ${SSH_OPTS} ${other_host} chkconfig puppet on

  heading "${other_host}: run_puppet.sh"
  ./run_puppet.sh ${other_host} &> run_puppet.${other_host}.log &
done
