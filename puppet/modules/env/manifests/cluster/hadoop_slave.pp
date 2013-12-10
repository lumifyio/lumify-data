class env::cluster::hadoop_slave {
  include my_fw
  class { 'hadoop::fw::datanode' :
    stage => 'first',
  }
  class { 'hadoop::fw::tasktracker' :
    stage => 'first',
  }

  include role::hadoop::slave
}
