class elasticsearch(
  $version = "1.4.1",
  $user = "elasticsearch",
  $group = "hadoop",
  $installdir = "/usr/share",
  $logdir = "/var/log/elasticsearch",
  $tmpdir = '/tmp',
  $elasticsearch_locations = hiera_array('elasticsearch_locations'),
  $index_shards = hiera('elasticsearch_index_shards', 2),
) {
  require repo::elasticsearch

  package { 'elasticsearch':
    ensure  => installed,
  }

  $homedir = "${installdir}/elasticsearch"
  $configdir = "/etc/elasticsearch"

  if $interfaces =~ /bond0/ {
    $es_node_ip = $ipaddress_bond0
  } elsif $interfaces =~ /eth1/ {
    $es_node_ip = $ipaddress_eth1
  } else {
    $es_node_ip = $ipaddress_eth0
  }

  file { "elasticsearch-env-config":
    path    => "${configdir}/elasticsearch.yml",
    ensure  => file,
    content => template("elasticsearch/elasticsearch-${version}.yml.erb"),
    require => Package["elasticsearch"],
  }

  file { "elasticsearch-logging-config":
    path    => "${configdir}/logging.yml",
    ensure  => file,
    content => template("elasticsearch/logging-${version}.yml.erb"),
    require => Package["elasticsearch"],
  }

  macro::download { "https://oss.sonatype.org/service/local/repositories/releases/content/org/securegraph/securegraph-elasticsearch-plugin/0.6.0/securegraph-elasticsearch-plugin-0.6.0.zip":
    path    => "${tmpdir}/securegraph-elasticsearch-plugin-0.6.0.zip",
  } -> exec { "securegraph-install" :
    command => "${homedir}/bin/plugin --url file://${tmpdir}/securegraph-elasticsearch-plugin-0.6.0.zip --install securegraph",
    cwd     => "${homedir}",
    creates => "${homedir}/plugins/securegraph",
    require => Package['elasticsearch'],
  }

  macro::download { "https://github.com/mobz/elasticsearch-head/archive/master.zip":
    path    => "${tmpdir}/head-plugin.zip",
  } -> exec { "head-install" :
    command => "${homedir}/bin/plugin --url file://${tmpdir}/head-plugin.zip --install head",
    cwd     => "${homedir}",
    creates => "${homedir}/plugins/head",
    require => Package['elasticsearch'],
  }

  define setup_data_directory ($user, $group) {
    file { [ "${name}/elasticsearch", "${name}/elasticsearch/data", "${name}/elasticsearch/work" ] :
      ensure  => directory,
      owner   => $user,
      group   => $group,
      mode    => 'u=rwx,g=rwx,o=',
      require =>  [ Package['elasticsearch'], File[$name] ],
    }
  }

  $data_dir_list = split($data_directories, ',')

  setup_data_directory { $data_dir_list :
    user => $user,
    group => $group,
  }
}
