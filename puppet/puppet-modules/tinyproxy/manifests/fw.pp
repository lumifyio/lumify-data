class tinyproxy::fw {
  firewall { '008 allow tinyproxy' :
    proto  => tcp,
    port   => 8080,
    action => accept,
  }
}
