class accumulo(
  $version = "1.6.1",
  $user = "accumulo",
  $group = "hadoop",
  $installdir = "/opt",
  $bindir = "/opt/accumulo/bin",
  $logdir = "/opt/accumulo/logs",
  $tmpdir = '/tmp'
) {
  include macro
  require cloudera::cdh5::hadoop::base

  $accumulo_masters = hiera_array('accumulo_masters')
  $accumulo_slaves = hiera_array('accumulo_slaves')
  $accumulo_example_config = hiera('accumulo_example_config')
  $zookeeper_nodes = hiera_hash('zookeeper_nodes')
  $namenode_ipaddress = hiera("namenode_ipaddress")
  $namenode_hostname = hiera("namenode_hostname")

  $homedir = "${installdir}/accumulo-${version}"
  $homelink = "${installdir}/accumulo"
  $configdir = "/opt/accumulo-${version}/conf"
  $configlink = "/opt/accumulo/conf"
  $downloadfile = "accumulo-${version}-bin.tar.gz"
  $downloadpath = "${tmpdir}/${downloadfile}"

  if $interfaces =~ /eth1/ {
    $accumulo_host_address = $ipaddress_eth1
  } else {
    $accumulo_host_address = $ipaddress_eth0
  }

  user { $user :
    ensure  => "present",
    gid     => $group,
    home    => $configlink,
    require => Package[$cloudera::cdh5::hadoop::base::pkg],
  }

  macro::download { "http://archive.apache.org/dist/accumulo/${version}/${downloadfile}":
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

  exec { "copy-example-accumulo-config" :
    command => "/bin/cp ${homedir}/conf/examples/${accumulo_example_config}/* ${configdir}",
    unless  => "/usr/bin/test -f ${configdir}/accumulo-env.sh",
    require => [Macro::Extract[$downloadpath]],
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
  $accumulo_instance_secret = hiera('accumulo_instance_secret', 'DEFAULT')
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
