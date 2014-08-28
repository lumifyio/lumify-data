# == Class: ganglia::gmetad::config
#
# Responsible for component configuration
#
class ganglia::gmetad::config {

  file { "${::ganglia::gangliaDir}/gmetad.conf" :
    ensure  => present,
    mode    => '0644',
    content => template("${module_name}/gmetad.conf.erb"),
    notify  => Class['ganglia::gmetad::service'],
  }
}
