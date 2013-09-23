class env::cluster::accumulo_master {
  package { 'hadoop-zookeeper' :
    ensure => present,
  }

  include my_fw
  include accumulo::fw::master
  include accumulo::fw::gc
  include accumulo::fw::monitor

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
