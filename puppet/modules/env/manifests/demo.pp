class env::demo {
  require buildtools
  require java

  include repo::cloudera::cdh4

  include ::zookeeper
  include role::hadoop::pseudo
  include role::accumulo::pseudo
  include ::hue

  include role::elasticsearch::pseudo

  include ::kafka
  include role::storm::master
  include role::storm::supervisor

  # TODO: configure firewall rules
  service { 'iptables' :
    enable => false,
    ensure => 'stopped',
  }

  file { '/opt/start.sh' :
    source => 'puppet:///modules/env/dev/start.sh',
    owner => 'vagrant',
    mode => 'u=rwx,g=,o=',
  }

  file { '/opt/status.sh' :
    source => 'puppet:///modules/env/dev/status.sh',
    owner => 'vagrant',
    mode => 'u=rwx,g=,o=',
  }

  file { '/opt/stop.sh' :
    source => 'puppet:///modules/env/dev/stop.sh',
    owner => 'vagrant',
    mode => 'u=rwx,g=,o=',
  }

  file { '/opt/format.sh' :
    source => 'puppet:///modules/env/dev/format.sh',
    owner => 'vagrant',
    mode => 'u=rwx,g=,o=',
  }

  file { '/opt/shell.sh' :
    source => 'puppet:///modules/env/dev/shell.sh',
    owner => 'vagrant',
    mode => 'u=rwx,g=,o=',
  }


  file { '/opt/storm-kill.sh' :
    source => 'puppet:///modules/env/dev/storm-kill.sh',
    owner => 'vagrant',
    mode => 'u=rwx,g=,o=',
  }

  file { '/opt/storm-run.sh' :
    source => 'puppet:///modules/env/dev/storm-run.sh',
    owner => 'vagrant',
    mode => 'u=rwx,g=,o=',
  }

  file { '/opt/kafka-clear.sh' :
    source => 'puppet:///modules/env/dev/kafka-clear.sh',
    owner => 'vagrant',
    mode => 'u=rwx,g=,o=',
  }

  file { '/etc/sysctl.conf' :
    source => 'puppet:///modules/env/dev/sysctl.conf',
    owner => 'root',
    mode => 'u=rw,g=r,o=r',
  }

  file { '/etc/hosts' :
    source => 'puppet:///modules/env/dev/hosts',
    owner => 'vagrant',
    mode => 'u=rwx,g=r,o=r',
  }
}
