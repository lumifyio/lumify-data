class role::hadoop::datanode {
  include ::hadoop::tasktracker
  include ::hadoop::datanode
  include ::zookeeper
}