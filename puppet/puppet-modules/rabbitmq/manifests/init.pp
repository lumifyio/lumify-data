class rabbitmq {
  require erlang
	
  $rabbitMQVersion = "3.2.3"
  
  $plugins = ["rabbitmq_management"]

  package { 'rabbitmq-server' :
		ensure   => installed,
		provider => "rpm",
		source   => "http://www.rabbitmq.com/releases/rabbitmq-server/v${rabbitMQVersion}/rabbitmq-server-${rabbitMQVersion}-1.noarch.rpm",
  }
  
  define installPlugin {
    exec { "${title}" :
      command     => "/usr/sbin/rabbitmq-plugins enable ${title}",
      user        => "root",
      group       => "root",      
      environment => ["HOME=/root"],
      require     => Package['rabbitmq-server'],
    }
  }
  
  installPlugin { "${plugins}" : }
}