class env::cluster {
  include env::common::config

  class { '::ntp' :
    servers => [ 'puppet' ],
  }
}
