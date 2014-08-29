class ganglia::meta(
  $ganglia_cluster_name = hiera('ganglia_cluster_name'),
  $ganglia_server_ip = hiera('ganglia_server_ip'),
) inherits ganglia {

  package { 'ganglia-gmetad':
    ensure => present,
  }

  file { "/etc/ganglia/gmetad.conf" :
    ensure  => file,
    content => template("ganglia/gmetad.conf.erb"),
    require => Package['ganglia-gmetad'],
  }

  service { 'gmetad' :
    enable  => true,
    ensure  => running,
    require => [
      Package['ganglia-gmetad'],
      File['gmetad-conf'],
    ],
  }
}
