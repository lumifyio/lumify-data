class env::cluster::hadoop_secondary {
  include my_fw
  class { 'hadoop::fw::secondarynamenode' :
    stage => 'first',
  }

  include role::hadoop::secondarynamenode
}
