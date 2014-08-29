class ganglia::mon(
  $ganglia_cluster_name = hiera('ganglia_cluster_name'),
  $ganglia_server_ip = hiera('ganglia_server_ip'),
) inherits ganglia {
  require buildtools::epel

  package { [ 'ganglia-gmond', 'ganglia-gmond-python' ] :
    ensure => present,
    require => Exec['epel'],
  }

  file { '/etc/ganglia/gmond.conf' :
    ensure  => file,
    content => template("ganglia/gmond.conf.erb"),
    require => Package['ganglia-gmond'],
  }

  service { 'gmond' :
    enable  => true,
    ensure  => running,
    require => [
      Package['ganglia-gmond'],
      File['/etc/ganglia/gmond.conf'],
    ],
  }

  include ganglia::mon::diskstat
}
