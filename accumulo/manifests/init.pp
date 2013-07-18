class accumulo(
  $version="1.4.3",
  $user="accumulo",
  $group="hadoop",
  $installdir="/usr/lib",
  $logdir="/var/log/accumulo"
) {
  include macro
  require hadoop

  $homedir = "${installdir}/accumulo-${version}"
  $homelink = "${installdir}/accumulo"
  $configdir = "/etc/accumulo-${version}"
  $configlink = "/etc/accumulo"
  $downloaddir = "/tmp/downloads"
  $downloadpath = "${downloaddir}/accumulo-${version}-dist.tar.gz"

  notify { "Installing Accumulo ${version}. Please run `sudo -u ${user} ${homedir}/bin/accumulo init` to initialize after installation completes.":}

  user { $user :
    ensure  => "present",
    gid     => $group,
    home    => $configlink,
    require => Group[$group],
  }

  file { $downloaddir:
    ensure => directory,
    mode   => 777,
  }

  macro::download { "http://www.us.apache.org/dist/accumulo/${version}/accumulo-${version}-dist.tar.gz":
    path    => $downloadpath,
    require => [File[$downloaddir], User[$user]],
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

  file { "${configdir}/accumulo-env.sh":
    ensure  => file,
    content => template("accumulo/accumulo-env.sh.erb"),
    require => Exec["copy-example-accumulo-config"],
  }

  file { "${configdir}/accumulo-site.xml":
    ensure  => file,
    content => template("accumulo/accumulo-site.xml.erb"),
    require => Exec["copy-example-accumulo-config"],
  }

  exec { 'change-config-file-modes':
    command => '/bin/find . -type f -exec chmod 0644 {} \;',
    cwd     => $configdir,
    require => Exec["copy-example-accumulo-config"],
  }

  file { $logdir:
    ensure => directory,
    owner  => 'root',
    group  => $group,
    mode   => 0775,
  }

  exec { "vm.swappiness=10 online" :
    command => "/sbin/sysctl -w vm.swappiness=10",
    unless  => "/usr/bin/test $(/sbin/sysctl -n vm.swappiness) -eq 10",
  }

  exec { "vm.swappiness=10 persistant" :
    command => '/bin/echo "vm.swappiness=10" >> /etc/sysctl.conf',
    unless  => "/bin/grep -q vm.swappiness=10 /etc/sysctl.conf",
  }
}
