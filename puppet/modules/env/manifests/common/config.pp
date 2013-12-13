class env::common::config {
  file { [ '/opt', '/opt/lumify', '/opt/lumify/config' ] :
    ensure => directory,
  }

  file { '/opt/lumify/config/log4j.xml' :
    ensure => file,
    source => 'puppet:///modules/env/cluster/log4j.xml',
    require => File['/opt/lumify/config'],
  }

  $hadoop_masters = hiera_array('hadoop_masters')
  $hadoop_slaves = hiera_array('hadoop_slaves')
  $zookeeper_nodes = hiera_hash('zookeeper_nodes')
  $accumulo_masters = hiera_array('accumulo_masters')
  $accumulo_slaves = hiera_array('accumulo_slaves')
  $elasticsearch_locations = hiera_array('elasticsearch_locations')
  $kafka_host_ipaddresses = hiera_hash('kafka_host_ipaddresses')
  $storm_supervisor_slots_ports = hiera_array('storm_supervisor_slots_ports')
  file { '/opt/lumify/config/configuration.properties' :
    ensure => file,
    content => template('env/cluster/configuration.properties.erb'),
    require => File['/opt/lumify/config'],
  }

  file { '/opt/lumify/config/credentials.properties-EXAMPLE' :
    ensure => file,
    source => 'puppet:///modules/env/cluster/credentials.properties-EXAMPLE',
    require => File['/opt/lumify/config'],
  }
}
