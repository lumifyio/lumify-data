class role::hadoop::master {
  include ::cloudera::cdh5::hadoop::namenode
  include ::cloudera::cdh5::hadoop::yarn::resourcemanager
  include ::cloudera::cdh5::hadoop::yarn::historyserver
}
