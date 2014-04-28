class rabbitmq {
  require erlang
  include macro
	
  $rabbitMQVersion = "3.2.3"
  
  $plugins = ["rabbitmq_management"]

  macro::download { "http://www.rabbitmq.com/releases/rabbitmq-server/v${rabbitMQVersion}/rabbitmq-server-${rabbitMQVersion}-1.noarch.rpm" :
    path     => "/tmp/rabbitmq-server-${rabbitMQVersion}-1.noarch.rpm",
  } -> package { 'rabbitmq-server' :
		ensure   => installed,
		provider => "rpm",
		source   => "/tmp/rabbitmq-server-${rabbitMQVersion}-1.noarch.rpm",
  }
  
  service { 'rabbitmq-server' :
    ensure  => running,
    enable  => true,
    require => Exec[$plugins],
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
