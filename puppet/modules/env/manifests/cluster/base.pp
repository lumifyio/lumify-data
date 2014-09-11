class env::cluster::base {
  include env::common::config
  include ganglia::mon

  include my_fw
  class { 'ganglia::fw' :
    stage => 'first',
  }

  class { '::ntp' :
    servers => [ 'puppet' ],
  }

  $java_version = hiera('java_version')
  class { 'java' :
    version => $java_version,
  }
}
