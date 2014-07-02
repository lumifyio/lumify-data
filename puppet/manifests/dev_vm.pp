stage { 'first' :
  before => Stage['main'],
}

class { env::dev :
  stage => 'first',
}
include env::demo
include env::common::logstash_config
include logstash::client
include logstash::ui
include clavin