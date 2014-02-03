class env::cluster::storm_supervisor inherits env::cluster::base {
  include my_fw
  class { 'storm::fw::supervisor' :
    stage => 'first',
  }

  include role::storm::supervisor

  file { '/opt/storm/lib/imageio-1.1.jar' :
    source => 'puppet:///modules/env/common/imageio-1.1.jar',
    owner => 'storm',
    mode => 'u=r,g=r,o=r',
  }
}
