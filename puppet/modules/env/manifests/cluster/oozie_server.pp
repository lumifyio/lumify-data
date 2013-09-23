class env::cluster::oozie_server {
  include my_fw
  include oozie::fw::server

  include oozie
}
