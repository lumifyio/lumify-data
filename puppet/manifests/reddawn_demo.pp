node "ip-10-0-3-10" {
  class { buildtools::epel :
    proxy_url => 'disabled',
  }

  package { "tinyproxy" :
    ensure => present,
    require => Exec["epel"],
  }

  exec { "tinyproxy-configure-port" :
    command => "/bin/sed -i 's/Port 8888/Port 8080/' /etc/tinyproxy/tinyproxy.conf",
    unless => "/bin/grep -q 'Port 8080' /etc/tinyproxy/tinyproxy.conf",
    require => Package["tinyproxy"],
  }

  exec { "tinyproxy-configure-loglevel" :
    command => "/bin/sed -i 's/LogLevel Info/LogLevel Connect/' /etc/tinyproxy/tinyproxy.conf",
    unless => "/bin/grep -q 'LogLevel Connect' /etc/tinyproxy/tinyproxy.conf",
    require => Package["tinyproxy"],
  }

  exec { "tinyproxy-configure-allow" :
    command => "/bin/sed -i 's|Allow 127.0.0.1|Allow 10.0.3.0/24|' /etc/tinyproxy/tinyproxy.conf",
    unless => "/bin/grep -q 'Allow 10.0.3.0/24' /etc/tinyproxy/tinyproxy.conf",
    require => Package["tinyproxy"],
  }

  service { "tinyproxy" :
    enable => true,
    ensure => running,
    require => [ Exec["tinyproxy-configure-port"], Exec["tinyproxy-configure-allow"] ],
  }

  include my_fw

  firewall { '008 allow tinyproxy' :
    proto  => tcp,
    port   => 8080,
    action => accept,
  }

  firewall { '009 allow puppetmaster' :
    proto  => tcp,
    port   => 8140,
    action => accept,
  }
}

node "ip-10-0-3-50" {
  include my_fw
  include hadoop::fw::namenode
  include hadoop::fw::jobtracker
  include blur::fw::controller
  include oozie::fw::server
  include role::hadoop::namenode # includes jobtracker
  include role::blur::controller
  include oozie
}

node "ip-10-0-3-51" {
  package { 'hadoop-zookeeper' :
    ensure => present,
  }
  include my_fw
  include hadoop::fw::secondarynamenode
  include accumulo::fw::master
  include accumulo::fw::gc
  include accumulo::fw::monitor
  include role::hadoop::secondarynamenode
  include role::accumulo::head
}

node "ip-10-0-3-101" {
  include my_fw
  include hadoop::fw::datanode
  include hadoop::fw::tasktracker
  include zookeeper::fw::node
  include accumulo::fw::tserver
  include accumulo::fw::logger
  include blur::fw::shard
  include elasticsearch::fw::node
  include role::hadoop::datanode # includes zookeeper, tasktracker, and native tools
  include role::accumulo::node
  include role::blur::shard
  include elasticsearch
}

node "ip-10-0-3-102" {
  include my_fw
  include hadoop::fw::datanode
  include hadoop::fw::tasktracker
  include zookeeper::fw::node
  include accumulo::fw::tserver
  include accumulo::fw::logger
  include blur::fw::shard
  include elasticsearch::fw::node
  include role::hadoop::datanode
  include role::accumulo::node
  include role::blur::shard
  include elasticsearch
}

node "ip-10-0-3-103" {
  include my_fw
  include hadoop::fw::datanode
  include hadoop::fw::tasktracker
  include zookeeper::fw::node
  include accumulo::fw::tserver
  include accumulo::fw::logger
  include blur::fw::shard
  include elasticsearch::fw::node
  include role::hadoop::datanode
  include role::accumulo::node
  include role::blur::shard
  include elasticsearch
}

node "ip-10-0-3-200" {
  include my_fw
  include jetty::fw::server
  include role::web::server

  $hadoop_masters = hiera_array('hadoop_masters')
  $hadoop_slaves = hiera_array('hadoop_slaves')
  $zookeeper_nodes = hiera_hash('zookeeper_nodes')
  $elasticsearch_locations = hiera_array('elasticsearch_locations')

  file { [ '/opt', '/opt/reddawn', '/opt/reddawn/config' ] :
    ensure => directory,
  }

  file { '/opt/reddawn/config/configuration.properties' :
    ensure => file,
    content => template('env/cluster/configuration.properties.erb'),
    require => File['/opt/reddawn/config'],
  }

  file { '/opt/reddawn/config/credentials.properties-EXAMPLE' :
    ensure => file,
    source => 'puppet:///modules/env/cluster/credentials.properties-EXAMPLE',
    require => File['/opt/reddawn/config'],
  }
}
