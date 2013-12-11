class env::cluster::elasticsearch_node {
  include my_fw
  class { 'elasticsearch::fw::node' :
    stage => 'first',
  }

  include elasticsearch
}
