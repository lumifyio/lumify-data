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
    require => Package[$hadoop::pkg],
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
    unless  => "/usr/bin/test -f ${configdir}/accumulo-env.sh",
    require => [Macro::Extract[$downloadpath], File[$configdir]],
  }

  file { "${homedir}/conf":
    ensure  => link,
    target  => $configdir,
    force   => true,
    require => Exec["copy-example-accumulo-config"],
  }

  define setup_walog_directory ($user, $group) {
    # /data[0-9] should be created by our hadoop dependency

    file { [ "${name}/accumulo", "${name}/accumulo/walog" ] :
      ensure  => directory,
      owner   => $user,
      group   => $group,
      mode    => 'u=rwx,g=rx,o=',
      require =>  [ File[$name], User[$user], Group[$group] ],
    }
  }

  $data_dir_list = split($data_directories, ',')

  setup_walog_directory { $data_dir_list :
    user => $user,
    group => $group,
  }

  define templated_config_file ($configdir) {
    file { "${configdir}/${name}" :
      ensure => file,
      content => template("accumulo/${name}.erb"),
    }
  }

  $hadoop_prefix = hiera('hadoop_home')
  $java_home = hiera('java_home')
  $zookeeper_home = '/usr/lib/zookeeper'
  $accumulo_instance_secret = 'DEFAULT'
  $accumulo_root_password = 'password'

  templated_config_file { [
      'accumulo-env.sh',
      'accumulo-site.xml',
      'masters',
      'monitor',
      'gc',
      'tracers',
      'slaves'
    ] :
    configdir => $configdir,
    require => Exec["copy-example-accumulo-config"],
  }

  define config_file ($configdir) {
    file { "${configdir}/${name}" :
      ensure => file,
      source => "puppet:///modules/accumulo/${name}",
    }
  }

  config_file { [
      'generic_logger.xml',
      'monitor_logger.xml'
    ] :
    configdir => $configdir,
    require => Exec["copy-example-accumulo-config"],
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
