class accumulo(
  $version = "1.5.0",
  $user = "accumulo",
  $group = "hadoop",
  $installdir = "/usr/lib",
  $bindir = "/usr/lib/accumulo/bin",
  $logdir = "/var/log/accumulo",
  $tmpdir = '/tmp'
) {
  include macro
  require hadoop

  $accumulo_masters = hiera_array('accumulo_masters')
  $accumulo_slaves = hiera_array('accumulo_slaves')
  $accumulo_example_config = hiera('accumulo_example_config')
  $zookeeper_nodes = hiera_hash('zookeeper_nodes')
  $namenode_ipaddress = hiera("namenode_ipaddress")
  $namenode_hostname = hiera("namenode_hostname")

  $homedir = "${installdir}/accumulo-${version}"
  $homelink = "${installdir}/accumulo"
  $configdir = "/etc/accumulo-${version}"
  $configlink = "/etc/accumulo"
  $downloadfile = "accumulo-${version}-bin.tar.gz"
  $downloadpath = "${tmpdir}/${downloadfile}"

  if $interfaces =~ /eth1/ {
    $accumulo_host_address = $ipaddress_eth1
  } else {
    $accumulo_host_address = $ipaddress_eth0
  }

  notify { "Installing Accumulo ${version}. Please run `sudo -u ${user} ${homedir}/bin/accumulo init` to initialize after installation completes.":}

  user { $user :
    ensure  => "present",
    gid     => $group,
    home    => $configlink,
    require => Package["hadoop.x86_64"],
  }

  macro::download { "http://apache.mirrors.tds.net/accumulo/${version}/${downloadfile}":
    path    => $downloadpath,
    require => User[$user],
  } -> macro::extract { $downloadpath:
    path    => $installdir,
    creates => "${installdir}/accumulo-${version}",
  }

  file { $homelink:
    ensure  => link,
    target  => $homedir,
    force => true,
    require => Macro::Extract[$downloadpath],
  }

  file { $configdir:
    ensure => directory,
  }

  file { $configlink:
    ensure => link,
    target => $configdir,
    require => File[$configdir],
  }

  exec { "copy-example-accumulo-config" :
    command => "/bin/cp ${homedir}/conf/examples/${accumulo_example_config}/* ${configdir}",
    user    => root,
    group   => root,
    unless  => "/usr/bin/test -f ${configdir}/accumulo-env.sh",
    require => [Macro::Extract[$downloadpath], File[$configdir]],
  }

  file { "${homedir}/conf":
    ensure  => link,
    target  => $configdir,
    force   => true,
    require => Exec["copy-example-accumulo-config"],
  }

  file { "accumulo-env-config":
    path    => "${configdir}/accumulo-env.sh",
    ensure  => file,
    content => template("accumulo/accumulo-env.sh.erb"),
    require => Exec["copy-example-accumulo-config"],
  }

  file { "accumulo-site-config":
    path    => "${configdir}/accumulo-site.xml",
    ensure  => file,
    content => template("accumulo/accumulo-site.xml.erb"),
    require => Exec["copy-example-accumulo-config"],
  }

  file { "accumulo-masters-config":
    path    => "${configdir}/masters",
    ensure  => file,
    content => template("accumulo/masters.erb"),
    require => Exec["copy-example-accumulo-config"],
  }

  file { "accumulo-monitor-config":
    path    => "${configdir}/monitor",
    ensure  => file,
    content => template("accumulo/monitor.erb"),
    require => Exec["copy-example-accumulo-config"],
  }

  file { "accumulo-gc-config":
    path    => "${configdir}/gc",
    ensure  => file,
    content => template("accumulo/gc.erb"),
    require => Exec["copy-example-accumulo-config"],
  }

  file { "accumulo-slaves-config":
    path    => "${configdir}/slaves",
    ensure  => file,
    content => template("accumulo/slaves.erb"),
    require => Exec["copy-example-accumulo-config"],
  }

  file { "${configdir}/generic_logger.xml" :
    ensure  => file,
    source  => 'puppet:///modules/accumulo/generic_logger.xml',
    require => Exec['copy-example-accumulo-config'],
  }

  file { "${configdir}/monitor_logger.xml" :
    ensure  => file,
    source  => 'puppet:///modules/accumulo/monitor_logger.xml',
    require => Exec['copy-example-accumulo-config'],
  }

  exec { 'change-accumulo-config-file-modes':
    command => '/bin/find ./* -type f -exec chmod 0644 {} \;',
    cwd     => $configdir,
    require => [File["accumulo-env-config"], File["accumulo-site-config"], File["accumulo-masters-config"], File["accumulo-gc-config"], File["accumulo-slaves-config"]],
  }

  file { $logdir:
    ensure => directory,
    owner  => $user,
    group  => $group,
  }

  file { "${homedir}/logs":
    ensure  => link,
    target  => $logdir,
    force   => true,
    require => [Macro::Extract[$downloadpath], File[$logdir]],
  }

  exec { "vm.swappiness=10 online" :
    command => "/sbin/sysctl -w vm.swappiness=10",
    unless  => "/usr/bin/test $(/sbin/sysctl -n vm.swappiness) -eq 10",
  }

  exec { "vm.swappiness=10 persistant" :
    command => '/bin/echo "vm.swappiness=10" >> /etc/sysctl.conf',
    unless  => "/bin/grep -q vm.swappiness=10 /etc/sysctl.conf",
  }

  file { "${configdir}/.ssh":
    ensure  => directory,
    owner   => $user,
    group   => $group,
    mode    => 0755,
    require => Macro::Extract[$downloadpath],
  }

  macro::setup-passwordless-ssh { $user :
    sshdir  => "${configdir}/.ssh",
    require => File["${configdir}/.ssh"],
  }
}
