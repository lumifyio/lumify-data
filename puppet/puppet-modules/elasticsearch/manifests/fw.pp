class elasticsearch::fw::node {
  firewall { '051 allow elasticsearch' :
    port   => [9200, 9300],
    proto  => tcp,
    action => accept,
  }
}
