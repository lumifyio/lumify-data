node "ip-10-0-3-10" {
  notify { "i am the puppet master" : }
}

node "ip-10-0-3-50" {
  include role::hadoop::namenode # includes jobtracker
  include role::blur::controller
}

node "ip-10-0-3-51" {
  include role::hadoop::secondarynamenode
  include role::accumulo::head
}

node "ip-10-0-3-101" {
  include role::hadoop::datanode # includes zookeeper, tasktracker, and native tools
  include role::accumulo::node
  include role::blur::shard
}

node "ip-10-0-3-102" {
  include role::hadoop::datanode
  include role::accumulo::node
  include role::blur::shard
}

node "ip-10-0-3-103" {
  include role::hadoop::datanode
  include role::accumulo::node
  include role::blur::shard
}

node "ip-10-0-3-200" {
  include role::webapp
}
