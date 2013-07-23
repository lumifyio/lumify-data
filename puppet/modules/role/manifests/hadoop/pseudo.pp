class role::hadoop::pseudo {
  include namenode
  include secondarynamenode
  include datanode
  include ::hadoop::config::pseudo
}