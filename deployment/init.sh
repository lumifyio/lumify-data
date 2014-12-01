#!/bin/bash -eu

PUPPETLABS_RPM_NAME='puppetlabs-release-6-7.noarch'
PUPPETLABS_RPM_URL="http://yum.puppetlabs.com/el/6/products/i386/${PUPPETLABS_RPM_NAME}.rpm"
SSH_OPTS='-o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o LogLevel=QUIET'

HOSTS_FILE=$1

set +u
[ "${PROXY_URL}" != '' ] || PROXY_URL=http://$(hostname):8080
set -u

function heading {
  local text=$1

  echo -n $'\n\e[01;35m'
  echo ${text}
  echo -n $'\e[00;35m'
  printf '%*s\n' "${COLUMNS:-$(tput cols)}" | tr ' ' -
  echo -n $'\e[00;00m'
}

function setup_local {
  heading 'configure enviornment name resolution'
  grep -q $(hostname) /etc/hosts || cat ${HOSTS_FILE} >> /etc/hosts

  heading 'install LVM'
  yum -y install lvm2
  # TODO: reboot?

  heading 'add the PuppetLabs yum repo, install and enable puppet'
  rpm -q ${PUPPETLABS_RPM_NAME} || rpm -ivh ${PUPPETLABS_RPM_URL}
  yum -y install puppet-server-3.6.0-1.el6.noarch
  # TODO: check if a version > 3.6.1 unbreaks things (https://tickets.puppetlabs.com/browse/PUP-2659)
  chkconfig puppetmaster on
  chkconfig puppet on

  heading 'setup SSL certs'
  [ -f /var/lib/puppet/ssl/certs/$(hostname).pem ] || puppet cert generate $(hostname) --dns_alt_names=puppet
  awk -v localhost=$(hostname) '$2!=localhost {print $2}' ${HOSTS_FILE} > /etc/puppet/autosign.conf

  heading 'insert firewall rules for the puppermaster and tinyproxy services'
  local rule_number=$(iptables -L -n --line-numbers | awk '/tcp dpt:22/ {print $1}')
  iptables -I INPUT ${rule_number} -p tcp -m state --state NEW -m tcp --dport 8140 -j ACCEPT
  rule_number=$(iptables -L -n --line-numbers | awk '/tcp dpt:22/ {print $1}')
  iptables -I INPUT ${rule_number} -p tcp -m state --state NEW -m tcp --dport 8080 -j ACCEPT
  service iptables save
  # TODO: use puppet to configure iptables now since it will later

  heading 'configure puppet'
  cat >> /etc/puppet/puppet.conf <<EO_PUPPET_CONF

[master]
    modulepath = \$confdir/modules:/usr/share/puppet/modules:\$confdir/lumify-modules:\$confdir/puppet-modules
EO_PUPPET_CONF

  heading 'install PuppetLabs modules'
  puppet module list | grep -q puppetlabs-firewall || puppet module install puppetlabs-firewall --version 0.4.2
  puppet module list | grep -q puppetlabs-ntp || puppet module install puppetlabs-ntp

  heading 'install our configuration and modules, start the puppetmaster service'
  ./update.sh start

  heading 'run puppet once in the forground'
  puppet agent -t || true

  heading 'run puppet as a service'
  service puppet start
}

function stage_jobtracker {
  heading 'stage artifacts on the jobtracker'
  local jobtracker_host=$(awk '/jobtracker/ {print $1}' ${HOSTS_FILE})
  scp ${SSH_OPTS} config-*.tgz \
                  setup_config.sh \
                  setup_import.sh \
                  ${jobtracker_host}:
}

function stage_www {
  heading 'stage artifacts on the webserver(s)'
  for www_host in $(awk '/www/ {print $1}' ${HOSTS_FILE}); do
    scp ${SSH_OPTS} *.xml \
                    *.war \
                    ${www_host}:
  done
}

function setup_remote {
  local other_host=$1

  heading "${other_host}: configure enviornment name resolution"
  scp ${SSH_OPTS} ${HOSTS_FILE} ${other_host}:
  ssh ${SSH_OPTS} ${other_host} "grep -q ${other_host} /etc/hosts || cat ${HOSTS_FILE} >> /etc/hosts"

  heading "${other_host}: configure yum to use the proxy"
  cat <<EO_YUM_CONF | ssh ${SSH_OPTS} ${other_host} 'cat >> /etc/yum.conf'

proxy=${PROXY_URL}
EO_YUM_CONF

  heading "${other_host}: install LVM"
  ssh ${SSH_OPTS} ${other_host} yum -y install lvm2
  # TODO: reboot?

  heading "${other_host}: setup EBS disks"
  scp ${SSH_OPTS} setup_disks.sh ${other_host}:
  ssh ${SSH_OPTS} ${other_host} './setup_disks.sh ebs 2>&1 | tee setup_disks.ebs.log'

  heading "${other_host}: setup instance store disks"
  ssh ${SSH_OPTS} ${other_host} './setup_disks.sh instance 2>&1 | tee setup_disks.instance.log'

  heading "ensure /data0 direcotry"
  ssh ${SSH_OPTS} ${other_host} mkdir -p /data0

  heading "${other_host}: disable IPv6"
  ssh ${SSH_OPTS} ${other_host} sysctl -w net.ipv6.conf.all.disable_ipv6=1
  cat <<EO_SYSCTL_CONF | ssh ${SSH_OPTS} ${other_host} 'cat >> /etc/sysctl.conf'

net.ipv6.conf.all.disable_ipv6 = 1
EO_SYSCTL_CONF

  heading "${other_host}: add the PuppetLabs yum repo, install and enable puppet"
  ssh ${SSH_OPTS} ${other_host} "rpm -q ${PUPPETLABS_RPM_NAME} || http_proxy=${PROXY_URL} rpm -ivh ${PUPPETLABS_RPM_URL}"
  ssh ${SSH_OPTS} ${other_host} yum -y install puppet-3.6.0-1.el6.noarch
  # TODO: check if a version > 3.6.1 unbreaks things (https://tickets.puppetlabs.com/browse/PUP-2659)

  ssh ${SSH_OPTS} ${other_host} chkconfig puppet on

  heading "${other_host}: run_puppet.sh"
  ./run_puppet.sh ${other_host} &> run_puppet.${other_host}.log &
}

function setup_other {
  for other_host in $(awk -v localhost=$(hostname) '$2!=localhost {print $1}' ${HOSTS_FILE}); do
    setup_remote ${other_host}
  done
}


set +u
[ "$2" ] && mode_or_ip=$2 || mode_or_ip=everything
set -u
case ${mode_or_ip} in
  local)
    setup_local
    ;;
  jt)
    stage_jobtracker
    ;;
  www)
    stage_www
    ;;
  everything)
    setup_local
    stage_jobtracker
    stage_www
    setup_other
    ;;
  stage)
    stage_jobtracker
    stage_www
    ;;
  other)
    setup_other
    ;;
  *.*.*.*)
    setup_remote ${mode_or_ip}
    ;;
  *)
    echo "invalid mode or ip: ${mode_or_ip}"
    exit -1
    ;;
esac
