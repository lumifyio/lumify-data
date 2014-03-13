class zookeeper {
  require repo::cloudera::cdh4

  package { 'zookeeper-server':
    ensure  => installed,
    require => Class['java', 'repo::cloudera::cdh4'],
  }

  $zookeeper_nodes = hiera_hash('zookeeper_nodes')

  if $interfaces =~ /bond0/ {
    $zookeeper_node_ip = $ipaddress_bond0
  } elsif $interfaces =~ /eth1/ {
    $zookeeper_node_ip = $ipaddress_eth1
  } elsif $interfaces =~ /em2/ {
    $zookeeper_node_ip = $ipaddress_em2
  } else {
    $zookeeper_node_ip = $ipaddress_eth0
  }

  file { "/var/log/zookeeper":
    ensure => "directory",
    owner  => "zookeeper",
    require => Package['zookeeper-server'],
  }

  file { '/etc/zookeeper/conf/zoo.cfg':
    ensure  => file,
    content => template('zookeeper/zoo.cfg.erb'),
    require => Package['zookeeper-server'],
  }

  exec { 'initialize-zookeeper' :
    path => "/sbin",
    command => 'service zookeeper-server init --force',
    creates => '/var/lib/zookeeper/version-2',
    require => Package['zookeeper-server'],
  }

  file { '/var/lib/zookeeper/myid':
    ensure  => file,
    content => template('zookeeper/myid.erb'),
    mode    => 'u=rw,go=r',
    require => [
      Exec['initialize-zookeeper'],
      Package['zookeeper-server'],
    ],
  }
}
