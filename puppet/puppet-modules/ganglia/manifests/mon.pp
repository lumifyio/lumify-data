class ganglia::mon(
  $cluster_name=hiera('ganglia_cluster_name'),
  $cluster_service_ip=hiera('ganglia_cluster_service_ip'),
) inherits ganglia {

  package { 'ganglia-gmond':
    ensure => present,
  }

  file { "gmond-conf":
    path    => "/etc/ganglia/gmond.conf",
    ensure  => file,
    content => template("ganglia/gmond.conf.erb"),
    require => Package['ganglia-gmond'],
  }

  service { 'gmond' :
    enable  => true,
    ensure  => running,
    require => [
      Package['ganglia-gmond'],
      File['gmond-conf'],
    ],
  }
}
