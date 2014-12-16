stage { 'first' :
  before => Stage['main'],
}

class { env::dev :
  stage => 'first',
}
include env::demo

# Disable logstash elasticsearch until we can get two elasticsearch instances to run on the same machine
#include env::common::logstash_config
#include logstash::client
#include logstash::ui

include clavin
include env::common::webserver
