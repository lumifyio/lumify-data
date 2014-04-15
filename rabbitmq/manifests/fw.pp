class rabbitmq::fw::ui {
  firewall { '505 allow rabbitmq ui' :
    port   => [15672],
    proto  => tcp,
    action => accept,
  }
}

class rabbitmq::fw::amqp (
  $srcnet = "0.0.0.0/0" ){
    
  firewall { '504 allow rabbitmq amqp' :
    port   => [5672],
    proto  => tcp,
    action => accept,
    source => "${srcnet}"
  }
  
}