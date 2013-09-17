node "nic-hadoop-smmc01.nearinfinity.com" {
  include env::cluster::puppetmaster
}

node "nic-hadoop-smmc02.nearinfinity.com" {
  include env::cluster::hadoop_master
  include env::cluster::oozie_server
}

node "nic-hadoop-smmc03.nearinfinity.com" {
  include env::cluster::hadoop_secondary
  include env::cluster::accumulo_master
}

node "nic-hadoop-smmc04.nearinfinity.com" {
  include env::cluster::node
  include env::cluster::zookeeper_server
}

node "nic-hadoop-smmc05.nearinfinity.com" {
  include env::cluster::node
  include env::cluster::zookeeper_server
}

node "nic-hadoop-smmc06.nearinfinity.com" {
  include env::cluster::node
  include env::cluster::zookeeper_server
}

node "nic-hadoop-smmc07.nearinfinity.com" {
  include env::cluster::node
}

node "nic-hadoop-smmc08.nearinfinity.com" {
  include env::cluster::webserver
}
