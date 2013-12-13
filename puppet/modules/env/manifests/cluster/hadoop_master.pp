class env::cluster::hadoop_master inherits env::cluster {
  include my_fw
  class { 'hadoop::fw::namenode' :
    stage => 'first',
  }
  class { 'hadoop::fw::jobtracker' :
    stage => 'first',
  }

  include role::hadoop::master # includes namenode and jobtracker
}
