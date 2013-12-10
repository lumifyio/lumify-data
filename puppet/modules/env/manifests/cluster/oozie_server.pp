class env::cluster::oozie_server {
  include my_fw
  class { 'oozie::fw::server' :
    stage => 'first',
  }

  include oozie
}
