node "ip-10-0-3-10" {
  include env::cluster::puppetmaster
}

node "ip-10-0-3-50" {
  include env::cluster::hadoop_master
  include env::cluster::oozie_server
}

node "ip-10-0-3-51" {
  include env::cluster::hadoop_secondary
  include env::cluster::accumulo_master
}

node "ip-10-0-3-101" {
  include env::cluster::node
  include env::cluster::zookeeper_server
}

node "ip-10-0-3-102" {
  include env::cluster::node
  include env::cluster::zookeeper_server
}

node "ip-10-0-3-103" {
  include env::cluster::node
  include env::cluster::zookeeper_server
}

node "ip-10-0-3-104" {
  include env::cluster::node
}

node "ip-10-0-3-200" {
  include env::cluster::webserver
}
