class blur (
  $version="0.2.0-incubating-SNAPSHOT",
  $user="blur",
  $group="hadoop",
  $installdir="/usr/lib",
  $logdir="/var/log/apache-blur",
  $piddir="/var/run/apache-blur",
  $javahome="/usr/java/default",
  $hadoophome="/usr/lib/hadoop",
  $tmpdir = '/tmp'
) {
  include macro
  require hadoop

  $java_home = hiera("java_home")
  $hadoop_home = hiera("hadoop_home")

  $homedir = "${installdir}/apache-blur-${version}"
  $homelink = "${installdir}/apache-blur"
  $configdir = "/etc/apache-blur-${version}"
  $configlink = "/etc/apache-blur"
  $downloadpath = "${tmpdir}/apache-blur-${version}-bin.tar.gz"

  user { $user :
    ensure  => "present",
    gid     => $group,
    home    => $configlink,
    require => Package["hadoop-0.20"],
  }

  file { $downloadpath:
    ensure  => file,
    source  => "puppet:///modules/blur/apache-blur-0.2.0-incubating-SNAPSHOT-bin.tar.gz",
  }

  macro::extract { $downloadpath:
    path    => $installdir,
    require => File[$downloadpath],
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
    ensure  => link,
    target  => $configdir,
    require => File[$configdir],
  }

  exec { "copy-config" :
    command => "/bin/cp ${homedir}/conf/* ${configdir}",
    user    => root,
    group   => root,
    unless  => "/usr/bin/test -f ${configdir}/blur-env.sh",
    require => [Macro::Extract[$downloadpath], File[$configdir]],
  }

  file { "${configdir}/blur-env.sh":
    ensure  => file,
    content => template("blur/blur-env.sh.erb"),
    require => Exec["copy-config"],
  }

  exec { 'change-blur-config-file-modes':
    command => '/bin/find ./* -type f -exec chmod 0644 {} \;',
    cwd     => $configdir,
    require => Exec["copy-config"],
  }

  file { $logdir:
    ensure  => directory,
    owner   => 'root',
    group   => $group,
    mode    => 0775,
  }

  file { $piddir:
    ensure  => directory,
    owner   => 'root',
    group   => $group,
    mode    => 0775,
  }

  file { "${homedir}/pids":
    ensure  => link,
    target  => $piddir,
    force   => true,
    require => [Macro::Extract[$downloadpath], File[$piddir]],
  }

  file { "${homedir}/logs":
    ensure  => link,
    target  => $logdir,
    force   => true,
    require => [Macro::Extract[$downloadpath], File[$logdir]],
  }

  exec { 'ulimit -Sn 50000' :
    command => '/bin/echo "* soft nofile 50000" >> /etc/security/limits.conf',
    unless => '/bin/grep -q "* soft nofile 50000" /etc/security/limits.conf',
  }

  exec { 'ulimit -Hn 100000' :
    command => '/bin/echo "* hard nofile 100000" >> /etc/security/limits.conf',
    unless  => '/bin/grep -q "* hard nofile 100000" /etc/security/limits.conf',
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