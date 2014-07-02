class env::cluster::logstash inherits env::cluster::base {
  include my_fw
  class { 'elasticsearch::fw::node' :
    stage => 'first',
  }
  class { 'logstash::fw::ui' :
    stage => 'first',
  }

  class { 'elasticsearch' :
    elasticsearch_locations => [$ipaddress],
  }
  include logstash::ui
}
