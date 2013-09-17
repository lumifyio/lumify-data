class env::cluster::zookeeper_server {
  include my_fw
  include zookeeper::fw::node

  include zookeeper
}
