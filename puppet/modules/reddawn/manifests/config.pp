class reddawn::config {

  file { '/opt/start.sh' :
    source => 'puppet:///modules/reddawn/start.sh',
    mode => 'u=rwx,g=,o=',
  }

  file { '/opt/stop.sh' :
    source => 'puppet:///modules/reddawn/stop.sh',
    mode => 'u=rwx,g=,o=',
  }

  file { '/opt/format.sh' :
    source => 'puppet:///modules/reddawn/format.sh',
    mode => 'u=rwx,g=,o=',
  }

  file { '/opt/shell.sh' :
    source => 'puppet:///modules/reddawn/shell.sh',
    mode => 'u=rwx,g=,o=',
  }

}
