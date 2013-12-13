class env::cluster::oozie_server inherits env::cluster {
  include my_fw
  class { 'oozie::fw::server' :
    stage => 'first',
  }

  include oozie
}
