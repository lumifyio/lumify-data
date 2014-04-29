class rabbitmq::fw::ui {
  firewall { '505 allow rabbitmq ui' :
    port   => [15672],
    proto  => tcp,
    action => accept,
  }
}

class rabbitmq::fw::amqp (
  $srcnet = "0.0.0.0/0"
){
  firewall { '504 allow rabbitmq amqp' :
    port   => [5672],
    proto  => tcp,
    action => accept,
    source => "${srcnet}"
  }
}

class rabbitmq::fw::clustering ( # http://www.rabbitmq.com/clustering.html#firewall
  $srcnet = "0.0.0.0/0"
){
  firewall { '506 allow rabbitmq clustering' :
    port   => [4369, 25672],
    proto  => tcp,
    action => accept,
    source => "${srcnet}"
  }
}
