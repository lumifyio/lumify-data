# == Class: ganglia::gmond::service
#
# Responsible for component service installation
#
class ganglia::gmond::service {

  service { 'gmond' :
    ensure => running,
    enable => true,
  }
}
