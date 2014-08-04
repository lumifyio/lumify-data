stage { 'first' :
  before => Stage['main'],
}

class { env::dev :
  stage => 'first',
}
include env::demo

include clavin
include env::common::webserver
