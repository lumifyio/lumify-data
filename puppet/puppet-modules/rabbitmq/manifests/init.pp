class rabbitmq (
  $cluster_nodes = [],
  $erlang_cookie = '' # http://www.rabbitmq.com/clustering.html#setup
){
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

  if $erlang_cookie != '' {
    file { '/var/lib/rabbitmq/.erlang.cookie' :
      ensure  => file,
      content => $erlang_cookie,
      owner   => 'rabbitmq',
      group   => 'rabbitmq',
      mode    => 'u=r,go=',
      require => Package['rabbitmq-server'],
      before  => Service['rabbitmq-server'],
    }
  }

  file { '/etc/rabbitmq/rabbitmq.config' :
    ensure  => file,
    content => template('rabbitmq/rabbitmq.config.erb'),
    require => Package['rabbitmq-server'],
    before  => Service['rabbitmq-server'],
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

  define policy (
    $vhost = '%2f', # /
    $pattern,
    $apply_to = 'all',
    $definition,
    $priority = 0
  ) {
    $url = "http://localhost:15672/api/policies/${vhost}/${name}"
    $json = "{\"pattern\":\"${pattern}\", \"apply-to\":\"${apply_to}\", \"definition\":${definition}, \"priority\":${priority}}"

    exec { "add-policy-${name}" :
      command => "/usr/bin/curl -u guest:guest -H 'content-type:application/json' -X PUT ${url} -d '${json}'",
      unless  => "/usr/bin/curl -s -u guest:guest ${url} -w '%{http_code}\n' -o /dev/null | /bin/grep -q 200",
      require => Service['rabbitmq-server'],
    }
  }

  policy { "ha" :
    pattern => '.*',
    definition => '{"ha-mode":"all", "ha-sync-mode":"automatic"}',
  }
}
