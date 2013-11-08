class zookeeper {
  package { 'zookeeper-server':
    ensure  => installed,
    require => Package['hadoop.x86_64'],
  }

  $zookeeper_nodes = hiera_hash('zookeeper_nodes')

  if $interfaces =~ /eth1/ {
    $zookeeper_node_ip = $ipaddress_eth1
  } else {
    $zookeeper_node_ip = $ipaddress_eth0
  }

  file { 'hadoop-zookeeper-config':
    path    => '/etc/zookeeper/conf/zoo.cfg',
    ensure  => file,
    content => template('zookeeper/zoo.cfg.erb'),
    require => Package['zookeeper-server'],
  }

  file { "/var/zookeeper":
    ensure => "directory",
  }

  file { 'hadoop-zookeeper-myid':
    path    => '/var/zookeeper/myid',
    ensure  => file,
    content => template('zookeeper/myid.erb'),
    require => Package['zookeeper-server'],
  }
}
