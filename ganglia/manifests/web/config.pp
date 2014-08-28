# == Class: ganglia::web::config
#
# Responsible for component configuration
#
class ganglia::web::config {

  file { "${::ganglia::gangliaDir}/conf.php" :
    ensure  => present,
    mode    => '0644',
    content => template("${module_name}/conf.php.erb"),
  }

  $localnet = hiera('internal_network')
  file { '/etc/httpd/conf.d/ganglia.conf' :
    ensure  => present,
    mode    => '0644',
    content => template("${module_name}/ganglia.conf.erb"),
    notify  => Service['httpd']
  }
}
