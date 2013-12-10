class env::cluster::zookeeper_server {
  include my_fw
  class { 'zookeeper::fw::node' :
    stage => 'first',
  }

  include zookeeper
}
