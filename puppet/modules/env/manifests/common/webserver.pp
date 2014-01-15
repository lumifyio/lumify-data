class env::common::webserver {
  include role::web::server
  include config

  $hadoop_masters = hiera_array('hadoop_masters')
  $hadoop_slaves = hiera_array('hadoop_slaves')
  $zookeeper_nodes = hiera_hash('zookeeper_nodes')
  $elasticsearch_locations = hiera_array('elasticsearch_locations')
  $authentication_provider = hiera('authentication_provider')

  exec { 'create default java keystore' :
    command => '/usr/java/default/bin/keytool -genkeypair -keysize 2048 -keyalg RSA -keystore /opt/lumify/config/jetty.jks -keypass password -storepass password -dname CN=demo.lumify.io',
    creates => '/opt/lumify/config/jetty.jks',
    require => [ File['/opt/lumify/config'], Class[java] ],
    logoutput => true,
  }
}
