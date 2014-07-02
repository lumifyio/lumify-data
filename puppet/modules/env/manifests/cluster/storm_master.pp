class env::cluster::storm_master inherits storm_base {
  include my_fw
  class { 'storm::fw::nimbus' :
    stage => 'first',
  }
  class { 'storm::fw::ui' :
    stage => 'first',
  }

  include role::storm::master
}
