class env::cluster::storm_master inherits env::cluster::base {
  include my_fw
  class { 'storm::fw::nimbus' :
    stage => 'first',
  }
  class { 'storm::fw::ui' :
    stage => 'first',
  }

  include role::storm::master
}
