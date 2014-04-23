class env::cluster::rabbitmq_node inherits env::cluster::base {
  include my_fw
  class { 'rabbitmq::fw::ui' :
    stage => 'first',
  }
  class { 'rabbitmq::fw::amqp' :
    stage => 'first',
  }

  include role::rabbitmq::node
}