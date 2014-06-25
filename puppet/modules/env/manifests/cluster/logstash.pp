class env::cluster::logstash inherits env::cluster::base {
  include my_fw
  class { 'elasticsearch::fw::node' :
    stage => 'first',
  }

  class { 'elasticsearch' :
    elasticsearch_locations => [$ipaddress],
  }
  include role::logstash::ui
}