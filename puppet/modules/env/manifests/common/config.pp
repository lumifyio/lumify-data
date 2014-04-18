class env::common::config(
  $main_properties_filename = 'lumify.properties'
) {
  file { [ '/opt', '/opt/lumify', '/opt/lumify/config' ] :
    ensure => directory,
  }

  group { 'lumify' :
    ensure => present,
    name => 'lumify',
  }
  exec { 'group-lumify-add-jetty' :
    command => '/usr/sbin/usermod -a -G lumify jetty',
    returns => [ 0, 6 ],
    require => [ Group['lumify'] ],
  }
  exec { 'group-lumify-add-tomcat' :
    command => '/usr/sbin/usermod -a -G lumify tomcat',
    returns => [ 0, 6 ],
    require => [ Group['lumify'] ],
  }
  exec { 'group-lumify-add-storm' :
    command => '/usr/sbin/usermod -a -G lumify storm',
    returns => [ 0, 6 ],
    require => [ Group['lumify'] ],
  }
  exec { 'group-lumify-add-hdfs' :
    command => '/usr/sbin/usermod -a -G lumify hdfs',
    returns => [ 0, 6 ],
    require => [ Group['lumify'] ],
  }
  exec { 'group-lumify-add-mapred' :
    command => '/usr/sbin/usermod -a -G lumify mapred',
    returns => [ 0, 6 ],
    require => [ Group['lumify'] ],
  }

  file { '/opt/lumify/logs' :
    ensure => directory,
    group => 'lumify',
    mode => 'u=rwx,g=rwxs,o=rx',
    require => [ File['/opt/lumify'], Group['lumify'] ],
  }

  $syslog_server = hiera('syslog_server', '')
  $syslog_facility = 'local3'
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
      'log4j.xml',
      $main_properties_filename,
      'lumify-enterprise.properties',
      'lumify-clavin.properties'
    ] :
    require => File['/opt/lumify/config'],
  }
}
