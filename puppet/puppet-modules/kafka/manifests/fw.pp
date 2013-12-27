class kafka::fw::node {
  $kafka_jmx_port = hiera('kafka_jmx_port')
  firewall { '111 allow kafka' :
    port   => 9092,
    proto  => tcp,
    action => accept,
  } ->
  firewall { '112 allow kafka JMX' :
    port   => 10000,
    proto  => tcp,
    action => accept,
  }
}
