class accumulo::logger inherits accumulo {
  $service_name = 'logger'
  file { '/etc/init/accumulo-logger.conf':
    ensure  => file,
    content => template("accumulo/upstart.conf.erb")
  }
}
