class env::demo {
  require buildtools

  $java_version = hiera('java_version')
  class { 'java' :
    version => $java_version,
  }

  include env::common::config
  include repo::cloudera::cdh4
  include ::zookeeper
  include role::hadoop::pseudo
  include role::accumulo::pseudo
  include role::elasticsearch::pseudo
  include role::rabbitmq::node
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
    'shell.sh',
    'storm-run.sh',
    'storm-kill.sh'
  ] : }

  file { '/etc/sysctl.conf' :
    source => 'puppet:///modules/env/dev/sysctl.conf',
    owner => 'root',
    mode => 'u=rw,g=r,o=r',
  }

  resources { 'host':
    purge => true,
  }

  host { 'lumify-vm.lumify.io' :
    ip => $ipaddress_eth1,
    host_aliases => 'lumify-vm',
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

  package { 'zip' :
    ensure => present,
  }

  include env::dev::nodejs
}
