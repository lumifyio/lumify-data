class env::cluster::base {
  include env::common::config

  class { '::ntp' :
    servers => [ 'puppet' ],
  }
}
