class storm::supervisor inherits storm {
  $process_type = 'supervisor'
  file { '/etc/init/storm-supervisor.conf':
    ensure  => file,
    content => template("storm/upstart.conf.erb")
  }
}
