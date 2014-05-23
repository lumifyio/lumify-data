class storm::logviewer inherits storm {
  $process_type = 'logviewer'
  file { '/etc/init/storm-logviewer.conf':
    ensure  => file,
    content => template("storm/upstart.conf.erb")
  }
}
