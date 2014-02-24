class rabbitmq {
  require erlang
	
  $rabbitMQVersion = "3.2.3"

  package { 'rabbitmq' :
		ensure   => installed,
		provider => "rpm",
		source   => "http://www.rabbitmq.com/releases/rabbitmq-server/v${rabbitMQVersion}/rabbitmq-server-${rabbitMQVersion}-1.noarch.rpm",
  }
}