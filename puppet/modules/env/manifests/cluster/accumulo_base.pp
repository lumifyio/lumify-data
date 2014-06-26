class env::cluster::accumulo_base inherits env::cluster::base {
  package { 'hadoop-zookeeper' :
    ensure => present,
  }

  include env::common::logstash_config
  include logstash::client
  ensure_resource('logstash::client::group_membership', 'accumulo_base', {group => 'hadoop'})
}