class env::common::config {
  file { [ '/opt', '/opt/lumify', '/opt/lumify/config' ] :
    ensure => directory,
  }

  # TODO: change the permissions and make it more restrictive. Possible create a lumify group and add all the users to it
  file { '/opt/lumify/logs' :
    ensure => directory,
    mode => 'u=rwx,g=rwx,o=rwx',
    require => [ File['/opt/lumify'] ],
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
  $authentication_provider = hiera('authentication_provider')
  $clavin_index_dir = hiera('clavin_index_dir')

  define config_file {
    file { "/opt/lumify/config/${name}" :
      ensure => file,
      content => template("env/common/${name}.erb"),
    }
  }

  config_file { [
      'lumify.properties',
      'lumify-enterprise.properties',
      'lumify-clavin.properties'
    ] :
    require => File['/opt/lumify/config'],
  }
}
