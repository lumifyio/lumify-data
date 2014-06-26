class env::cluster::webserver inherits env::cluster::base {
  include my_fw
  class { 'jetty::fw::server' :
    stage => 'first',
  }

  include env::common::webserver

  include env::common::logstash_config
  include logstash::client
  ensure_resource('logstash::client::group_membership', 'webserver', {group => 'jetty'})
}
