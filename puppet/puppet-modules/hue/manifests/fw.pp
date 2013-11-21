class hue::fw::node {
  firewall { '360 allow hue' :
    port   => [8888],
    proto  => tcp,
    action => accept,
  }
}
