class rabbitmq::fw::ui {
  firewall { '505 allow rabbitmq ui' :
    port   => [15672],
    proto  => tcp,
    action => accept,
  }
}