class env::cluster::storm_supervisor {
  include my_fw
  class { 'storm::fw::supervisor' :
    site => 'first',
  }

  include role::storm::supervisor
}
