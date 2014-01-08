class kafka_rpm(
  $user = "kafka",
  $group = "kafka",
  $installdir = "/opt",
  $home = "/opt/kafka",
  $bindir = "/opt/kafka/bin",
  $logdir = "/opt/kafka/logs",
  $tmpdir = '/tmp'
) {
  include macro
  require java
  require zookeeper

  $zookeeper_nodes = hiera_hash('zookeeper_nodes')
  $kafka_consumer_group_id = hiera('kafka_consumer_group_id')
  $kafka_host_ipaddresses = hiera_hash('kafka_host_ipaddresses')
  $kafka_jmx_registry_port = hiera('kafka_jmx_registry_port')
  $kafka_jmx_objects_port = hiera('kafka_jmx_objects_port')

  if $interfaces =~ /eth1/ {
    $kafka_host_ip = $ipaddress_eth1
  } else {
    $kafka_host_ip = $ipaddress_eth0
  }

  $configdir = "${home}/config"

  package { [ 'lumify-kafka' ] :
    ensure => present,
    require => File['/etc/yum.repos.d/lumify.repo'],
  }

  group { $group :
    ensure => present,
  }

  user { $user :
    ensure  => present,
    gid     => $group,
    home    => $home,
    require => Group[$group],
  }

  file { $logdir:
    ensure => directory,
    owner  => $user,
    group  => $group,
    require => Package['lumify-kafka']
  }

  file { "/etc/init/kafka.conf":
    ensure   => file,
    content  => template('kafka_rpm/upstart.conf.erb'),
    require  => Package['lumify-kafka'],
  }

  file { "${configdir}/consumer.properties":
    ensure   => file,
    content  => template('kafka_rpm/consumer.properties.erb'),
    require  => Package['lumify-kafka'],
  }

  file { "${configdir}/log4j.properties":
    ensure   => file,
    content  => template('kafka_rpm/log4j.properties.erb'),
    require  => Package['lumify-kafka'],
  }

  file { "${configdir}/server.properties":
    ensure   => file,
    content  => template('kafka_rpm/server.properties.erb'),
    require  => Package['lumify-kafka'],
  }

  file { "${configdir}/zookeeper.properties":
    ensure   => file,
    content  => template('kafka_rpm/zookeeper.properties.erb'),
    require  => Package['lumify-kafka'],
  }
}


