class storm::drpc inherits storm {
  $process_type = 'drpc'
  file { '/etc/init/storm-drpc.conf':
    ensure  => file,
    content => template("storm/upstart.conf.erb")
  }
}
