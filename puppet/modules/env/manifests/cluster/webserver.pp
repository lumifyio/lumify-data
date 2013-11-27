class env::cluster::webserver {
  include my_fw
  include jetty::fw::server

  include env::common::webserver
}
