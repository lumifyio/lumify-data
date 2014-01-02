class env::cluster::webserver inherits env::cluster::base {
  include my_fw
  class { 'jetty::fw::server' :
    stage => 'first',
  }

  include env::common::webserver
}
