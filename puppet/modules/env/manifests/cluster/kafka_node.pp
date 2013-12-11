class env::cluster::kafka_node {
  include my_fw
  class { 'kafka::fw::node' :
    stage => 'first',
  }

  include role::kafka::node
}
