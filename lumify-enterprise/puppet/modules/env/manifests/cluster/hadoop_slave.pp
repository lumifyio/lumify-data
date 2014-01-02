class env::cluster::hadoop_slave inherits env::cluster::base {
  include my_fw
  class { 'hadoop::fw::datanode' :
    stage => 'first',
  }
  class { 'hadoop::fw::tasktracker' :
    stage => 'first',
  }

  include role::hadoop::slave
}
