class env::cluster::spark_master {
  include my_fw
  class { 'spark::fw::master' :
    stage => 'first',
  }

  include ::spark::standalone::master
}
