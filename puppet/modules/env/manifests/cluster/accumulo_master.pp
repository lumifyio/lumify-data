class env::cluster::accumulo_master inherits accumulo_base {
  include my_fw
  class { 'accumulo::fw::master' :
    stage => 'first',
  }
  class { 'accumulo::fw::gc' :
    stage => 'first',
  }
  class { 'accumulo::fw::monitor' :
    stage => 'first',
  }

  include role::accumulo::head

  file { '/opt/lumify/balance_accumulo.sh' :
    ensure => file,
    source => 'puppet:///modules/env/cluster/balance_accumulo.sh',
    require => File['/opt/lumify'],
  }
}
