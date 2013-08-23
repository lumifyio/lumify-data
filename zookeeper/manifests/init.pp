class zookeeper {
  package { 'hadoop-zookeeper-server':
    ensure  => installed,
    require => Package['hadoop-0.20'],
  }

  $zookeeper_nodes = hiera_hash('zookeeper_nodes')

  file { 'hadoop-zookeeper-config':
    path    => '/etc/zookeeper/zoo.cfg',
    ensure  => file,
    content => template('zookeeper/zoo.cfg.erb'),
    require => Package['hadoop-zookeeper-server'],
  }
}
