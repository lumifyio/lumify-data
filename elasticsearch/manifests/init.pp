class elasticsearch(
  $version = "0.90.2",
  $user = "esearch",
  $group = "hadoop",
  $installdir = "/usr/lib",
  $logdir = "/var/log/elasticsearch",
  $datadir = "/var/lib/elasticsearch",
  $tmpdir = '/tmp'
) {
  include macro

  $homedir = "${installdir}/elasticsearch-${version}"
  $homelink = "${installdir}/elasticsearch"
  $configdir = "/etc/elasticsearch-${version}"
  $configlink = "/etc/elasticsearch"
  $downloadpath = "${tmpdir}/elasticsearch-${version}.tar.gz"
  $indexdir = "${datadir}/data"
  $workdir = "${datadir}/work"
  $piddir = "/var/run/elasticsearch"

  user { $user :
    ensure  => "present",
    gid     => $group,
    home    => $configlink,
  }

  macro::download { "https://download.elasticsearch.org/elasticsearch/elasticsearch/elasticsearch-${version}.tar.gz":
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

  file { "${homedir}/config":
    ensure  => link,
    target  => $configdir,
    force   => true,
    require => [File[$configdir], Macro::Extract[$downloadpath]],
  }

  file { "elasticsearch-env-config":
    path    => "${configdir}/elasticsearch.yml",
    ensure  => file,
    content => template("elasticsearch/elasticsearch.yml.erb"),
    require => File["${homedir}/config"],
  }

  file { "elasticsearch-logging-config":
    path    => "${configdir}/logging.yml",
    ensure  => file,
    content => template("elasticsearch/logging.yml.erb"),
    require => File["${homedir}/config"],
  }

  exec { 'change-elasticsearch-config-file-modes':
    command => '/bin/find ./* -type f -exec chmod 0644 {} \;',
    cwd     => $configdir,
    require => [File["elasticsearch-env-config"], File["elasticsearch-logging-config"]],
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

  file { $datadir:
    ensure => directory,
    owner  => $user,
    group  => $group,
  }

  file { $indexdir:
    ensure  => directory,
    owner   => $user,
    group   => $group,
    require => File[$datadir],
  }

  file { $workdir:
    ensure  => directory,
    owner   => $user,
    group   => $group,
    require => File[$datadir],
  }

  file { $piddir:
    ensure  => directory,
    owner   => $user,
    group   => $group,
  }
}
