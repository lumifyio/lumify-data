class rabbitmq {
  require erlang
	
  $rabbitMQVersion = "3.2.3"
  
  $plugins = ["rabbitmq_management"]

  package { 'rabbitmq-server' :
		ensure   => installed,
		provider => "rpm",
		source   => "http://www.rabbitmq.com/releases/rabbitmq-server/v${rabbitMQVersion}/rabbitmq-server-${rabbitMQVersion}-1.noarch.rpm",
  }
  
  define enablePlugin {
    exec { "${title}" :
      command     => "/usr/sbin/rabbitmq-plugins enable ${title}",
      onlyif      => "/usr/bin/test `/usr/sbin/rabbitmq-plugins list -E ${title} | wc -l` -eq 0",
      user        => "root",
      group       => "root",      
      environment => ["HOME=/root"],
      require     => Package['rabbitmq-server'],
    }
  }
  
  enablePlugin { "${plugins}" : }
}
