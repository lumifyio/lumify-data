class elasticsearch(
  $version = "1.1.2",
  $user = "esearch",
  $group = "hadoop",
  $installdir = "/usr/lib",
  $logdir = "/var/log/elasticsearch",
  $tmpdir = '/tmp',
  $elasticsearch_locations = hiera_array('elasticsearch_locations')
) {
  include macro
  include macro::git
  require hadoop

  $homedir = "${installdir}/elasticsearch-${version}"
  $homelink = "${installdir}/elasticsearch"
  $configdir = "/etc/elasticsearch-${version}"
  $configlink = "/etc/elasticsearch"
  $downloadpath = "${tmpdir}/elasticsearch-${version}.tar.gz"
  $piddir = "/var/run/elasticsearch"

  if $interfaces =~ /bond0/ {
    $es_node_ip = $ipaddress_bond0
  } elsif $interfaces =~ /eth1/ {
    $es_node_ip = $ipaddress_eth1
  } else {
    $es_node_ip = $ipaddress_eth0
  }

  user { $user :
    ensure  => "present",
    gid     => $group,
    home    => $configlink,
  }

  macro::download { "https://download.elasticsearch.org/elasticsearch/elasticsearch/elasticsearch-${version}.tar.gz":
    path    => $downloadpath,
    require => User[$user],
  } -> macro::extract { $downloadpath:
    path    => $installdir,
    creates => "${installdir}/elasticsearch-${version}",
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
    content => template("elasticsearch/elasticsearch-${version}.yml.erb"),
    require => File["${homedir}/config"],
  }

  file { "elasticsearch-logging-config":
    path    => "${configdir}/logging.yml",
    ensure  => file,
    content => template("elasticsearch/logging-${version}.yml.erb"),
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

  file { $piddir:
    ensure  => directory,
    owner   => $user,
    group   => $group,
  }

  # TODO: unused?
  macro::git::clone { "elasticsearch-servicewrapper-clone":
    url     => "https://github.com/elasticsearch/elasticsearch-servicewrapper.git",
    path    => "${tmpdir}/elasticsearch-servicewrapper",
    options => "--depth 1",
  }

  # TODO: unused?
  exec { "copy-elasticsearch-servicewrapper" :
    command => "/bin/cp -r ${tmpdir}/elasticsearch-servicewrapper/service/ ${homedir}/bin",
    unless  => "/usr/bin/test -f ${homedir}/bin/service/",
    require => [ Macro::Git::Clone["elasticsearch-servicewrapper-clone"], Macro::Extract[$downloadpath] ],
  }

  # TODO: unused?
  file { "elasticsearch-service-config":
    path    => "${homedir}/bin/service/elasticsearch.conf",
    ensure  => file,
    content => template("elasticsearch/elasticsearch.conf.erb"),
    require => Exec["copy-elasticsearch-servicewrapper"],
  }

  file { "${homedir}/config/env.sh" :
    ensure  => file,
    content => template("elasticsearch/env.sh.erb"),
    require => Macro::Extract[$downloadpath],
  }

  file { '/etc/init/elasticsearch.conf':
    ensure  => file,
    content => template("elasticsearch/upstart.conf.erb")
  }

  macro::download { "https://oss.sonatype.org/service/local/repositories/releases/content/org/securegraph/securegraph-elasticsearch-plugin/0.6.0/securegraph-elasticsearch-plugin-0.6.0.zip":
    path    => "${tmpdir}/securegraph-elasticsearch-plugin-0.6.0.zip",
  } -> exec { "securegraph-install" :
    command => "${homedir}/bin/plugin --url file://${tmpdir}/securegraph-elasticsearch-plugin-0.6.0.zip --install securegraph",
    cwd     => "${homedir}",
    creates => "${homedir}/plugins/securegraph",
    require => Macro::Extract[$downloadpath],
  }

  macro::download { "https://github.com/mobz/elasticsearch-head/archive/master.zip":
    path    => "${tmpdir}/head-plugin.zip",
  } -> exec { "head-install" :
    command => "${homedir}/bin/plugin --url file://${tmpdir}/head-plugin.zip --install head",
    cwd     => "${homedir}",
    creates => "${homedir}/plugins/head",
    require => Macro::Extract[$downloadpath],
  }

  define setup_data_directory ($user, $group) {
    file { [ "${name}/elasticsearch", "${name}/elasticsearch/data", "${name}/elasticsearch/work" ] :
      ensure  => directory,
      owner   => $user,
      group   => $group,
      mode    => 'u=rwx,g=rwx,o=',
      require =>  [ File[$name], User[$user], Group[$group] ],
    }
  }

  $data_dir_list = split($data_directories, ',')

  setup_data_directory { $data_dir_list :
    user => $user,
    group => $group,
  }
}
