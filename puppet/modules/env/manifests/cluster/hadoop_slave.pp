class env::cluster::hadoop_slave {
  include my_fw
  include hadoop::fw::datanode
  include hadoop::fw::tasktracker

  include role::hadoop::slave
}
