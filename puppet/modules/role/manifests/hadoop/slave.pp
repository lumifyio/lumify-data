class role::hadoop::slave {
  include ::cloudera::cdh5::hadoop::yarn::nodemanager
  include ::cloudera::cdh5::hadoop::datanode
}
