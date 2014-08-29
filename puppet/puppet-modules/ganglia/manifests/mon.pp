class ganglia::mon(
  $ganglia_cluster_name = hiera('ganglia_cluster_name'),
  $ganglia_server_ip = hiera('ganglia_server_ip'),
) inherits ganglia {

  package { [ 'ganglia-gmond', 'ganglia-gmond-python' ] :
    ensure => present,
  }

  file { "/etc/ganglia/gmond.conf":
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

  include ganglia::mon::diskstat
}
