class env::demo {
  require buildtools

  $java_version = hiera('java_version')
  class { 'java' :
    version => $java_version,
  }

  include env::common::config
  include cloudera::cdh5::repo
  include ::zookeeper
  include role::hadoop::pseudo
  include role::accumulo::pseudo
  include role::elasticsearch::pseudo
  include role::rabbitmq::node

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
    'shell.sh'
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

  package { 'zip' :
    ensure => present,
  }

  include env::dev::nodejs
}
