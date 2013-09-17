class env::cluster::hadoop_master {
  include my_fw
  include hadoop::fw::namenode
  include hadoop::fw::jobtracker

  include role::hadoop::master # includes namenode and jobtracker
}
