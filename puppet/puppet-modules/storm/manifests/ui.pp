class storm::ui inherits storm {
  $process_type = 'ui'
  file { '/etc/init/storm-ui.conf':
    ensure  => file,
    content => template("storm/upstart.conf.erb")
  }
}
