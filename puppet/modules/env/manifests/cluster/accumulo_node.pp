class env::cluster::accumulo_node {
  package { 'hadoop-zookeeper' :
    ensure => present,
  }

  include my_fw
  include accumulo::fw::tserver
  include accumulo::fw::logger

  include role::accumulo::node
}
