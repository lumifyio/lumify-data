class role::hadoop::master {
  include ::hadoop::namenode
  include ::hadoop::jobtracker
}
