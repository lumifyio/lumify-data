include repo::cloudera::cdh3
include role::hadoop::pseudo
include role::accumulo::pseudo
include role::blur::pseudo
include role::oozie::pseudo

# TODO: configure firewall rules
service { 'iptables' :
  enable => false,
  ensure => 'stopped',
}

