class env::cluster::accumulo_master {
  package { 'hadoop-zookeeper' :
    ensure => present,
  }

  include my_fw
  class { 'accumulo::fw::master' :
    site => 'first',
  }
  class { 'accumulo::fw::gc' :
    site => 'first',
  }
  class { 'accumulo::fw::monitor' :
    site => 'first',
  }

  include role::accumulo::head

  file { '/opt/lumify' :
    ensure => directory,
  }

  file { '/opt/lumify/balance_accumulo.sh' :
    ensure => file,
    source => 'puppet:///modules/env/cluster/balance_accumulo.sh',
    require => File['/opt/lumify'],
  }
}
