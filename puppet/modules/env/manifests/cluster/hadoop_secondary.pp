class env::cluster::hadoop_secondary {
  include my_fw
  include hadoop::fw::secondarynamenode

  include role::hadoop::secondarynamenode
}
