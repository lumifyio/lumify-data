# == Class: ganglia::gmond::config
#
# Responsible for component configuration
#
class ganglia::gmond::config {

  file { "${::ganglia::gangliaDir}/gmond.conf" :
    ensure  => present,
    mode    => '0644',
    content => template("${module_name}/gmond.conf.erb"),
    notify  => Class['ganglia::gmond::service'],
  }
}
