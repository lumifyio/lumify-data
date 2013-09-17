class env::cluster::elasticsearch_node {
  include my_fw
  include elasticsearch::fw::node

  include elasticsearch
}
