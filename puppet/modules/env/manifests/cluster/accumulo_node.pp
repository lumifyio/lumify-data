class env::cluster::accumulo_node inherits env::cluster::base {
  package { 'hadoop-zookeeper' :
    ensure => present,
  }

  include my_fw
  class { 'accumulo::fw::tserver' :
    stage => 'first',
  }
  class { 'accumulo::fw::logger' :
    stage => 'first',
  }

  include role::accumulo::node
}
