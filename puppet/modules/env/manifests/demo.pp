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

  file { '/opt/lumify/start.sh' :
    source => 'puppet:///modules/env/dev/start.sh',
    owner => 'vagrant',
    mode => 'u=rwx,g=,o=',
  }

  file { '/opt/lumify/status.sh' :
    source => 'puppet:///modules/env/dev/status.sh',
    owner => 'vagrant',
    mode => 'u=rwx,g=,o=',
  }

  file { '/opt/lumify/stop.sh' :
    source => 'puppet:///modules/env/dev/stop.sh',
    owner => 'vagrant',
    mode => 'u=rwx,g=,o=',
  }

  file { '/opt/lumify/format.sh' :
    source => 'puppet:///modules/env/dev/format.sh',
    owner => 'vagrant',
    mode => 'u=rwx,g=,o=',
  }

  file { '/opt/lumify/shell.sh' :
    source => 'puppet:///modules/env/dev/shell.sh',
    owner => 'vagrant',
    mode => 'u=rwx,g=,o=',
  }

  file { '/opt/lumify/storm-kill.sh' :
    source => 'puppet:///modules/env/dev/storm-kill.sh',
    owner => 'vagrant',
    mode => 'u=rwx,g=,o=',
  }

  file { '/opt/lumify/storm-run.sh' :
    source => 'puppet:///modules/env/dev/storm-run.sh',
    owner => 'vagrant',
    mode => 'u=rwx,g=,o=',
  }

  file { '/opt/lumify/kafka-clear.sh' :
    source => 'puppet:///modules/env/common/kafka-clear.sh',
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

  package { [ 'nodejs', 'npm', 'zip' ] :
    ensure => present,
  }

  exec { 'npm-install-bower' :
    command => '/usr/bin/npm install -g bower',
    unless => '/usr/bin/npm list -g 2>/dev/null | /bin/grep bower@',
    require => Package['npm'],
  }

  exec { 'npm-install-grunt' :
    command => '/usr/bin/npm install -g grunt',
    unless => '/usr/bin/npm list -g 2>/dev/null | /bin/grep grunt@',
    require => Package['npm'],
  }

  exec { 'npm-install-grunt-cli' :
    command => '/usr/bin/npm install -g grunt-cli',
    unless => '/usr/bin/npm list -g 2>/dev/null | /bin/grep grunt-cli@',
    require => Package['npm'],
  }
}
