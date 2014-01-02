class env::cluster::storm_supervisor inherits env::cluster::base {
  include my_fw
  class { 'storm::fw::supervisor' :
    stage => 'first',
  }

  include role::storm::supervisor
}
