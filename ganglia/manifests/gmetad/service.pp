# == Class: ganglia::gmetad::service
#
# Responsible for component service installation
#
class ganglia::gmetad::service {

  service { 'gmetad' :
    ensure => running,
    enable => true,
  }
}
