class zookeeper::fw:node {
  firewall { '260 allow zookeeper' :
    port   => [2181, 2888, 3888],
    proto  => tcp,
    action => accept,
  }
}
