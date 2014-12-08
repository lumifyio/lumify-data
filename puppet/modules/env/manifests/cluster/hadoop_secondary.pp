class env::cluster::hadoop_secondary inherits hadoop_base {
  include my_fw
  class { 'cloudera::cdh5::hadoop::fw::secondarynamenode' :
    stage => 'first',
  }

  include role::hadoop::secondarynamenode
}
