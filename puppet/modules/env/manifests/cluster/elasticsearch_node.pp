class env::cluster::elasticsearch_node {
  include my_fw
  class { 'elasticsearch::fw::node' :
    site => 'first',
  }

  include elasticsearch
}
