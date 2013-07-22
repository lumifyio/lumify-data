class env::dev {
  include role::hadoop::pseudo
  include role::accumulo::pseudo
  include role::blur::pseudo
  include role::oozie::pseudo

  # TODO: configure firewall rules
  service { 'iptables' :
    enable => false,
    ensure => 'stopped',
  }

  file { '/opt/start.sh' :
    source => 'puppet:///modules/env/start.sh',
    mode => 'u=rwx,g=,o=',
  }

  file { '/opt/stop.sh' :
    source => 'puppet:///modules/env/stop.sh',
    mode => 'u=rwx,g=,o=',
  }

  file { '/opt/format.sh' :
    source => 'puppet:///modules/env/format.sh',
    mode => 'u=rwx,g=,o=',
  }

  file { '/opt/shell.sh' :
    source => 'puppet:///modules/env/shell.sh',
    mode => 'u=rwx,g=,o=',
  }
}
