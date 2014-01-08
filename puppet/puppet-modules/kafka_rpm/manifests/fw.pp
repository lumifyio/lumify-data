class kafka::fw::node {
  $kafka_jmx_registry_port = hiera('kafka_jmx_registry_port')
  $kafka_jmx_objects_port = hiera('kafka_jmx_objects_port')
  firewall { '111 allow kafka' :
    port   => 9092,
    proto  => tcp,
    action => accept,
  } ->
  firewall { '112 allow kafka JMX RMI registry' :
    port   => $kafka_jmx_registry_port,
    proto  => tcp,
    action => accept,
  } ->
  firewall { '113 allow kafka JMX RMI connection objects' :
    port   => $kafka_jmx_objects_port,
    proto  => tcp,
    action => accept,
  }
}
