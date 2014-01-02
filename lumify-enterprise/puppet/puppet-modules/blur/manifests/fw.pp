class blur::fw::controller {
  firewall { '021 allow blur controller' :
    port   => [40010, 40080],
    proto  => tcp,
    action => accept,
  }
}

class blur::fw::shard {
  firewall { '022 allow blur shard' :
    port   => [40020, 40090],
    proto  => tcp,
    action => accept,
  }
}
