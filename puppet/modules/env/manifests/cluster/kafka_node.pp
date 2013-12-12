class env::cluster::kafka_node {
  include my_fw
  class { 'kafka::fw::node' :
    stage => 'first',
  }

  include role::kafka::node

  file { '/opt/lumify' :
    ensure => directory,
  }

  file { '/opt/lumify/kafka-clear.sh' :
    source => 'puppet:///modules/env/common/kafka-clear.sh',
    owner => 'root',
    mode => 'u=rwx,g=,o=',
    require => File['/opt/lumify'],
  }

}
