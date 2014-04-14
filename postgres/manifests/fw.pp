class postgres::fw::db {
  firewall { '520 allow postgres standard' :
    port   => [5432],
    proto  => tcp,
    action => accept,
  }
}
