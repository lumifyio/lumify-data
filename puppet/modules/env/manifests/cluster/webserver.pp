class env::cluster::webserver {
  include my_fw
  include jetty::fw::server

  include role::web::server

  $hadoop_masters = hiera_array('hadoop_masters')
  $hadoop_slaves = hiera_array('hadoop_slaves')
  $zookeeper_nodes = hiera_hash('zookeeper_nodes')
  $elasticsearch_locations = hiera_array('elasticsearch_locations')
  $authentication_provider = hiera('authentication_provider')

  file { [ '/opt', '/opt/lumify', '/opt/lumify/config' ] :
    ensure => directory,
  }

  file { '/opt/lumify/logs' :
    ensure => directory,
    owner => 'jetty',
    group => 'jetty',
    mode => 'u=rwx,g=,o=',
    require => [ File['/opt/lumify'], User['jetty'] ],
  }

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

  exec { 'create default java keystore' :
    command => '/usr/java/default/bin/keytool -genkeypair -keysize 2048 -keyalg RSA -keystore /opt/lumify/config/jetty.jks -keypass password -storepass password -dname CN=demo.lumify.io',
    creates => '/opt/lumify/config/jetty.jks',
    require => File['/opt/lumify/config'],
    logoutput => true,
  }
}
