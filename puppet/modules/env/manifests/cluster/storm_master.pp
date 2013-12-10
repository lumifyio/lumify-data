class env::cluster::storm_master {
  include my_fw
  class { 'storm::fw::nimbus' :
    site => 'first',
  }
  class { 'storm::fw::ui' :
    site => 'first',
  }

  include role::storm::master
}
