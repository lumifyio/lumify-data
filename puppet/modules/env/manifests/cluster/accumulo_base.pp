class env::cluster::accumulo_base inherits env::cluster::base {
  package { 'hadoop-zookeeper' :
    ensure => present,
  }
}