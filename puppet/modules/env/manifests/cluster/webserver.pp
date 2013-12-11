class env::cluster::webserver {
  include my_fw
  class { 'jetty::fw::server' :
    stage => 'first',
  }

  include env::common::webserver
}
