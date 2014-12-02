class rabbitmq (
  $cluster_nodes = [],
  $remove_default_user = true,
  $erlang_cookie = '' # http://www.rabbitmq.com/clustering.html#setup
){
  require erlang
  include macro

  $rabbitMQVersion = "3.4.2"
  $mgmt_user = hiera('rabbitmq_mgmt_user','guest')
  $mgmt_user_pw = hiera('rabbitmq_mgmt_user_pw','guest')
  $mgmt_user_tags = join(hiera_array('rabbitmq_mgmt_user_tags',['administrator'])," ")

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
    require => Package["rabbitmq-server"],
  }

  exec { "create_mgmt_user" :
    command => "/usr/sbin/rabbitmqctl add_user ${mgmt_user} ${mgmt_user_pw}",
    unless  => "/usr/sbin/rabbitmqctl list_users | grep -q ${mgmt_user}",
    user    => 'root',
    require => Service['rabbitmq-server'],
  }

  exec { "set_mgmt_user_perms" :
    command     => "/usr/sbin/rabbitmqctl set_permissions ${mgmt_user} \".*\" \".*\" \".*\"",
    refreshonly => true,
    user        => 'root',
    subscribe   => Exec['create_mgmt_user'],
  }
  exec { "set_mgmt_user_tag" :
    command     => "/usr/sbin/rabbitmqctl set_user_tags ${mgmt_user} ${mgmt_user_tags}",
    refreshonly => true,
    user        => 'root',
    subscribe   => Exec['create_mgmt_user'],
  }

  if $remove_default_user {
    exec { "remove_default_user" :
      command => "/usr/sbin/rabbitmqctl delete_user guest",
      onlyif  => "/usr/sbin/rabbitmqctl list_users | grep -q guest",
      user    => 'root',
      require => Service['rabbitmq-server'],
    }
  }

  exec { 'rabbitmq_management' :
    command     => "/usr/sbin/rabbitmq-plugins enable rabbitmq_management",
    unless      => "/usr/sbin/rabbitmq-plugins list -E rabbitmq_management | grep -q rabbitmq_management",
    user        => "root",
    group       => "root",
    environment => ["HOME=/root"],
    require     => Service['rabbitmq-server'],
  }

  define policy (
    $vhost = '%2f', # /
    $user = 'guest',
    $password = 'guest',
    $pattern,
    $apply_to = 'all',
    $definition,
    $priority = 0
  ) {
    $url = "http://localhost:15672/api/policies/${vhost}/${name}"
    $json = "{\"pattern\":\"${pattern}\", \"apply-to\":\"${apply_to}\", \"definition\":${definition}, \"priority\":${priority}}"

    exec { "add-policy-${name}" :
      command => "/usr/bin/curl -u ${user}:${password} -H 'content-type:application/json' -X PUT ${url} -d '${json}'",
      unless  => "/usr/bin/curl -s -u ${user}:${password} ${url} -w '%{http_code}\n' -o /dev/null | /bin/grep -q 200",
      require => [ Service['rabbitmq-server'],
                   Exec['rabbitmq_management']
                 ],
    }
  }

  policy { "ha" :
    pattern    => '.*',
    user       => $mgmt_user,
    password   => $mgmt_user_pw,
    definition => '{"ha-mode":"all", "ha-sync-mode":"automatic"}',
  }
}
