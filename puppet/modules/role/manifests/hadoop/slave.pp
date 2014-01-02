class role::hadoop::slave {
  include ::hadoop::tasktracker
  include ::hadoop::datanode
}
