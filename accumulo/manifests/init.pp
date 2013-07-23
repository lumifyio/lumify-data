class accumulo(
  $version = "1.4.3",
  $user = "accumulo",
  $group = "hadoop",
  $installdir = "/usr/lib",
  $logdir = "/var/log/accumulo",
  $tmpdir = '/tmp'
) {
  include macro
  require hadoop

  $homedir = "${installdir}/accumulo-${version}"
  $homelink = "${installdir}/accumulo"
  $configdir = "/etc/accumulo-${version}"
  $configlink = "/etc/accumulo"
  $downloadpath = "${tmpdir}/accumulo-${version}-dist.tar.gz"

  notify { "Installing Accumulo ${version}. Please run `sudo -u ${user} ${homedir}/bin/accumulo init` to initialize after installation completes.":}

  user { $user :
    ensure  => "present",
    gid     => $group,
    home    => $configlink,
    require => Package["hadoop-0.20"],
  }

  macro::download { "http://www.us.apache.org/dist/accumulo/${version}/accumulo-${version}-dist.tar.gz":
    path    => $downloadpath,
    require => User[$user],
  } -> macro::extract { $downloadpath:
    path  => $installdir,
  }

  file { $homelink:
    ensure  => link,
    target  => $homedir,
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
    command => "/bin/cp ${homedir}/conf/examples/512MB/native-standalone/* ${configdir}",
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

  file { "accumulo-slaves-config":
    path    => "${configdir}/slaves",
    ensure  => file,
    content => template("accumulo/slaves.erb"),
    require => Exec["copy-example-accumulo-config"],
  }

  exec { 'change-accumulo-config-file-modes':
    command => '/bin/find ./* -type f -exec chmod 0644 {} \;',
    cwd     => $configdir,
    require => [File["accumulo-env-config"], File["accumulo-site-config"], File["accumulo-masters-config"], File["accumulo-slaves-config"]],
  }

  file { $logdir:
    ensure => directory,
#    owner  => "root",
    owner  => $user,
    group  => $group,
#    mode   => 0775,
  }

#  file { "walog-dir":
#    path    => "${logdir}/walog",
#    ensure  => directory,
#    owner   => "root",
#    group   => $group,
#    mode    => 0755,
#    require => File[$logdir],
#  }
#
#  file { "walog-lock-file":
#    path    => "${logdir}/walog/.lock",
#    ensure  => file,
#    owner   => "root",
#    group   => $group,
#    mode    => 0764,
#    require => File["walog-dir"],
#  }

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
    sshdir  => "$configdir/.ssh",
    require => File["${configdir}/.ssh"],
  }
}
