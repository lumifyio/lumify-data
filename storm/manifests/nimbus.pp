class storm::nimbus inherits storm {
  $process_type = 'nimbus'
  file { '/etc/init/storm-nimbus.conf':
    ensure  => file,
    content => template("storm/upstart.conf.erb")
  }
}
