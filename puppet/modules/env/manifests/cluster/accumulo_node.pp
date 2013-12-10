class env::cluster::accumulo_node {
  package { 'hadoop-zookeeper' :
    ensure => present,
  }

  include my_fw
  class { 'accumulo::fw::tserver' :
    site => 'first',
  }
  class { 'accumulo::fw::logger' :
    site => 'first',
  }

  include role::accumulo::node
}
