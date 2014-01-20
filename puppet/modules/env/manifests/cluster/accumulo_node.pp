class env::cluster::accumulo_node inherits accumulo_base {
  include my_fw
  class { 'accumulo::fw::tserver' :
    stage => 'first',
  }
  class { 'accumulo::fw::logger' :
    stage => 'first',
  }

  include role::accumulo::node
}
