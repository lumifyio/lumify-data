class env::cluster::kafka_node {
  include my_fw
  class { 'kafka::fw::node' :
    site => 'first',
  }

  include role::kafka::node
}
