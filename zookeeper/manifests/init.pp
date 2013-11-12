class zookeeper {
  package { 'zookeeper-server':
    ensure  => installed,
    require => Class['java', 'repo::cloudera::cdh4'],    
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

  exec { 'initialize-zookeeper' :
    path => "/sbin",
    command => 'service zookeeper-server init --force',
    creates => '/var/lib/zookeeper/version-2',
    require => Package['zookeeper-server'],
  }
  
  file { 'hadoop-zookeeper-myid':
    path    => '/var/lib/zookeeper/myid',
    ensure  => file,
    content => template('zookeeper/myid.erb'),
    require => [
      Exec['initialize-zookeeper'],
      Package['zookeeper-server'],
    ],
  }
}
