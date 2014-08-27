class ganglia::meta(
  $cluster_name=hiera('ganglia_cluster_name'),
  $cluster_service_ip=hiera('ganglia_cluster_service_ip'),
) inherits ganglia {

  package { 'ganglia-gmetad':
    ensure => present,
  }

  file { "gmetad-conf":
    path    => "/etc/ganglia/gmetad.conf",
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
