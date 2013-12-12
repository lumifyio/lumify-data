class env::cluster::storm_supervisor inherits env::cluster {
  include my_fw
  class { 'storm::fw::supervisor' :
    stage => 'first',
  }

  include role::storm::supervisor
}
