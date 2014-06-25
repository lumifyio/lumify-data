class env::cluster::accumulo_base inherits env::cluster::base {
  package { 'hadoop-zookeeper' :
    ensure => present,
  }

  include env::common::logstash_config
  include logstash::client
}