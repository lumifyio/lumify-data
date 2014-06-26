class env::cluster::zookeeper_server inherits env::cluster::base {
  include my_fw
  class { 'zookeeper::fw::node' :
    stage => 'first',
  }

  include zookeeper

  include env::common::logstash_config
  include logstash::client
  ensure_resource('logstash::client::group_membership', 'zookeeper_server', {group => 'zookeeper'})
}
