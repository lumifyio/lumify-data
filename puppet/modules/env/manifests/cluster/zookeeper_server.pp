class env::cluster::zookeeper_server inherits env::cluster {
  include my_fw
  class { 'zookeeper::fw::node' :
    stage => 'first',
  }

  include zookeeper
}
