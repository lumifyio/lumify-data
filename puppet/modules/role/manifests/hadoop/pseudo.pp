class role::hadoop::pseudo {
  include master
  include secondarynamenode
  include slave
  include ::hadoop::config::pseudo
}
