class env::cluster::hadoop_master inherits hadoop_base {
  include my_fw
  class { 'cloudera::cdh5::hadoop::fw::namenode' :
    stage => 'first',
  }
  class { 'cloudera::cdh5::hadoop::fw::resourcemanager' :
    stage => 'first',
  }
  class { 'cloudera::cdh5::hadoop::fw::historyserver' :
    stage => 'first',
  }

  include role::hadoop::master
}
