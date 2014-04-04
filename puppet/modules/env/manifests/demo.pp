class env::demo {
  require buildtools
  require java

  include env::common::config
  include repo::cloudera::cdh4
  include ::zookeeper
  include role::hadoop::pseudo
  include role::accumulo::pseudo
  include role::elasticsearch::pseudo
  include ::kafka
  include role::storm::master
  include role::storm::supervisor

  # no firewall for local vms
  service { 'iptables' :
    enable => false,
    ensure => 'stopped',
  }

  define install_script ($type='dev') {
    file { "/opt/lumify/${name}" :
      source => "puppet:///modules/env/${type}/${name}",
      owner => 'vagrant',
      mode => 'u=rwx,g=,o=',
    }
  }

  install_script { [
    'format.sh',
    'start.sh',
    'stop.sh',
    'status.sh',
    'shell.sh',
    'storm-run.sh',
    'storm-kill.sh'
  ] : }

  install_script { 'kafka-clear.sh' :
    type => 'common'
  }

  file { '/etc/sysctl.conf' :
    source => 'puppet:///modules/env/dev/sysctl.conf',
    owner => 'root',
    mode => 'u=rw,g=r,o=r',
  }

  resources { 'host':
    purge => true,
  }

  host { 'lumify-vm' :
    ip => $ipaddress_eth1,
    host_aliases => 'lumify-vm.lumify.io',
  }

  host { 'localhost' :
    ip => '127.0.0.1',
    host_aliases => ['localhost.localdomain', 'localhost4', 'localhost4.localdomain4'],
  }

  file { '/opt/storm/lib/imageio-1.1.jar' :
    source => 'puppet:///modules/env/common/imageio-1.1.jar',
    owner => 'storm',
    mode => 'u=r,g=r,o=r',
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
