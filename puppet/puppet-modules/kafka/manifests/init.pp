class kafka(
  $version = "0.7.2",
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
  require buildtools::epel

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

  $downloadpath = "${tmpdir}/kafka-${version}-incubating-src.tgz"
  $configdir = "${home}/config"
  $extractdir = "${installdir}/kafka-${version}-incubating-src"

  group { $group :
    ensure => present,
  }

  user { $user :
    ensure  => present,
    gid     => $group,
    home    => $home,
    require => Group[$group],
  }

  macro::download { "http://archive.apache.org/dist/kafka/old_releases/kafka-${version}-incubating/kafka-${version}-incubating-src.tgz":
    path    => $downloadpath,
    require => User[$user],
  } -> macro::extract { $downloadpath:
    path    => $installdir,
    creates => $extractdir,
  }

  file { $extractdir:
    ensure  => directory,
    owner   => $user,
    group   => $group,
    recurse => true,
    require => Macro::Extract[$downloadpath],
  }

  file { $home:
    ensure  => link,
    target  => $extractdir,
    require => File[$extractdir],
  }

  file { $logdir:
    ensure => directory,
    owner  => $user,
    group  => $group,
    require => File[$home]
  }

  $java_agent_jar = "${home}/jmx-rmi-agent-0.1.jar"
  file { "${java_agent_jar}" :
    ensure  => file,
    mode    => 'u=rw,go=r',
    source  => 'puppet:///modules/kafka/jmx-rmi-agent-0.1.jar',
    require => File["${home}"],
  }

  file { "/etc/init/kafka.conf":
    ensure   => file,
    content  => template('kafka/upstart.conf.erb'),
    require  => File[$home],
  }

  file { "${configdir}/consumer.properties":
    ensure   => file,
    content  => template('kafka/consumer.properties.erb'),
    require  => File[$home],
  }

  file { "${configdir}/log4j.properties":
    ensure   => file,
    content  => template('kafka/log4j.properties.erb'),
    require  => File[$home],
  }

  file { "${configdir}/producer.properties":
    ensure   => file,
    content  => template('kafka/producer.properties.erb'),
    require  => File[$home],
  }

  file { "${configdir}/server.properties":
    ensure   => file,
    content  => template('kafka/server.properties.erb'),
    require  => File[$home],
  }

  file { "${configdir}/zookeeper.properties":
    ensure   => file,
    content  => template('kafka/zookeeper.properties.erb'),
    require  => File[$home],
  }

  $java_home = hiera("java_home")
  exec { "sbt update" :
    command => "/bin/sh sbt update",
    user    => $user,
    group   => $group,
    cwd     => $extractdir,
    environment => "JAVA_HOME=${java_home}",
#    unless  => "/usr/bin/test -f ${home}/lib_managed",
    require => [
      File["${configdir}/consumer.properties"],
      File["${configdir}/log4j.properties"],
      File["${configdir}/producer.properties"],
      File["${configdir}/server.properties"],
      File["${configdir}/zookeeper.properties"]
    ],
  }

  exec { "sbt package" :
    command => "/bin/sh sbt package",
    user    => $user,
    group   => $group,
    cwd     => $extractdir,
    environment => "JAVA_HOME=${java_home}",
    #    unless  => "/usr/bin/test -f ${home}/lib_managed",
    require => Exec["sbt update"],
  }

  package { "expect" :
    ensure => present,
    require => Exec["epel"],
  }

  file { "$bindir/kafka-create-topic.sh":
    ensure   => file,
    source   => "puppet:///modules/kafka/kafka-create-topic.sh",
    owner    => "root",
    group    => "root",
    force    => true,
  }

  file { "$bindir/kafka-create-topic-helper.sh":
    ensure   => file,
    source   => "puppet:///modules/kafka/kafka-create-topic-helper.sh",
    owner    => "root",
    group    => "root",
    force    => true,
  }

  file { "$bindir/kafka-list-topic.sh":
    ensure   => file,
    source   => "puppet:///modules/kafka/kafka-list-topic.sh",
    owner    => "root",
    group    => "root",
    force    => true,
  }
}
