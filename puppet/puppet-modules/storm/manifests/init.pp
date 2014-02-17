######################################################################################################
# Based on instructions found at https://github.com/nathanmarz/storm/wiki/Setting-up-a-Storm-cluster
# Storm Installation Dependencies:
#   ZeroMQ 2.1.7
#	JZMQ
#	Java 6
#	Python 2.6.6
######################################################################################################
class storm(
  $version = "0.8.2",
  $user = "storm",
  $group = "storm",
  $installdir = "/opt",
  $home = "/opt/storm",
  $tmpdir = '/tmp',
  $zeromq_version = '2.1.7',
  $storm_local_dir = '/var/storm',
) {
  include macro
  include macro::git
  require java
  require zookeeper
  require buildtools

  $zookeeper_nodes = hiera_hash('zookeeper_nodes')
  $zookeeper_port = hiera('zookeeper_port')
  $storm_nimbus_host = hiera('storm_nimbus_host')
  $storm_nimbus_thrift_port = hiera('storm_nimbus_thrift_port')
  $storm_supervisor_slots_ports = hiera('storm_supervisor_slots_ports')
  $storm_ui_port = hiera('storm_ui_port')

  $downloadpath = "${tmpdir}/storm-${version}.zip"
  $extractdir = "${installdir}/storm-${version}"

  $make    = "/usr/bin/make"
  $make_multicore    = "${make} -j${processorcount}"
  $install = "/usr/bin/make install"

  group { $group :
    ensure => present,
  }

  user { $user :
    ensure  => present,
    gid     => $group,
    home    => $home,
    require => Group[$group],
  }

  # Required by Storm
  package { 'python':
    ensure  => installed
  }

  # Required to build zeromq
  package { 'libuuid-devel':
    ensure  => installed
  }

  macro::download { 'zeromq-download':
    url  => "http://download.zeromq.org/zeromq-${zeromq_version}.tar.gz",
    path => "${tmpdir}/zeromq-${zeromq_version}.tar.gz",
  } -> macro::extract { 'extract-zeromq':
    file    => "${tmpdir}/zeromq-${zeromq_version}.tar.gz",
    path    => $tmpdir,
    creates => "${tmpdir}/zeromq-${zeromq_version}",
  }

  exec { 'zeromq-build' :
    cwd         => "${tmpdir}/zeromq-${zeromq_version}",
    command     => "${tmpdir}/zeromq-${zeromq_version}/configure && ${make_multicore} && ${install}",
    creates     => "/usr/local/lib/libzmq.so",
    timeout     => 0,
    require     => [Macro::Extract['extract-zeromq'], Package['libuuid-devel']],
  }

  macro::git::clone { 'jzmq-clone':
    url     => 'https://github.com/nathanmarz/jzmq.git',
    path    => "${tmpdir}/jzmq",
    options => '--depth 1'
  }

  exec { 'jzmq-build' :
    cwd         => "${tmpdir}/jzmq",
    command     => "${tmpdir}/jzmq/autogen.sh && ${tmpdir}/jzmq/configure && ${make} && ${install}",
    creates     => '/usr/local/lib/libjzmq.so',
    environment => ["JAVA_HOME=/usr/java/default"], 
    timeout     => 0,
    require     => [ Macro::Git::Clone['jzmq-clone'], Exec['zeromq-build'] ],
  }

  macro::download { "https://www.dropbox.com/s/fl4kr7w0oc8ihdw/storm-${version}.zip":
    path    => $downloadpath,
    require => [Package['python'], Exec['jzmq-build']],
  } -> macro::extract { $downloadpath:
    type    => 'zip',
    path    => $installdir,
    creates => $extractdir,
  }

  $java_agent_jar = "${home}/jmx-rmi-agent-0.1.jar"
  file { "${java_agent_jar}" :
    ensure  => file,
    mode    => 'u=rw,go=r',
    source  => 'puppet:///modules/storm/jmx-rmi-agent-0.1.jar',
    require => File["${home}"],
  }

  file { "${extractdir}/conf/storm.yaml":
    ensure  => file,
    content => template("storm/storm.yaml.erb"),
    require => Macro::Extract[$downloadpath],
  }

  file { "${extractdir}/log4j/storm.log.properties":
    ensure  => file,
    source  => 'puppet:///modules/storm/storm.log.properties',
    require => Macro::Extract[$downloadpath],
  }

  file { "${extractdir}/lib/commons-io-2.4.jar" :
    ensure  => file,
    source  => 'puppet:///modules/storm/commons-io-2.4.jar',
    require => Macro::Extract[$downloadpath],
  }

  file { "${extractdir}/lib/commons-io-1.4.jar" :
    ensure  => absent,
    require => File["${extractdir}/lib/commons-io-2.4.jar"],
  }

  # Ensure the the local working dir looks ok
  file { $storm_local_dir:
    ensure  => directory,
    owner   => $user,
    group   => $group,
    require => User[$user],
  }

  # Ensure that the unzipped distro is owned by the proper user
  file { $extractdir:
    ensure  => directory,
    owner   => $user,
    group   => $group,
    recurse => true,
    require => [ Macro::Extract[$downloadpath], User[$user] ],
  }

  # Ensure that the storm user's home points to the distro
  file { $home:
    ensure  => link,
    target  => $extractdir,
    require => File[$extractdir],
  }
}
