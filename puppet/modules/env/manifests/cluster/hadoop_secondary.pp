class env::cluster::hadoop_secondary inherits env::cluster {
  include my_fw
  class { 'hadoop::fw::secondarynamenode' :
    stage => 'first',
  }

  include role::hadoop::secondarynamenode
}
