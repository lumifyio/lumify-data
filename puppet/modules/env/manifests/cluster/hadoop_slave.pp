class env::cluster::hadoop_slave inherits hadoop_base {
  include my_fw
  class { 'cloudera::cdh5::hadoop::fw::datanode' :
    stage => 'first',
  }
  class { 'cloudera::cdh5::hadoop::fw::nodemanager' :
    stage => 'first',
  }

  include role::hadoop::slave
}
