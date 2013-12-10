class env::cluster::hadoop_master {
  include my_fw
  class { 'hadoop::fw::namenode' :
    site => 'first',
  }
  class { 'hadoop::fw::jobtracker' :
    site => 'first',
  }

  include role::hadoop::master # includes namenode and jobtracker
}
