class env::cluster::elasticsearch_node inherits env::cluster::base {
  include my_fw
  class { 'elasticsearch::fw::node' :
    stage => 'first',
  }

  include elasticsearch

  include env::common::logstash_config
  include logstash::client
  ensure_resource('logstash::client::group_membership', 'elasticsearch_node', {group => 'hadoop'})
}
