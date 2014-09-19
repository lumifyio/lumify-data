class env::cluster::spark_worker {
  include my_fw
  class { 'spark::fw::worker' :
    stage => 'first',
  }

  include ::spark::standalone::worker
}
