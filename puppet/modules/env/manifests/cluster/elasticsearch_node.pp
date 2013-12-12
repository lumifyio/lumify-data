class env::cluster::elasticsearch_node inherits env::cluster {
  include my_fw
  class { 'elasticsearch::fw::node' :
    stage => 'first',
  }

  include elasticsearch
}
