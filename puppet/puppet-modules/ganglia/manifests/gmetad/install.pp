# == Class: ganglia::gmetad::install
#
# Responsible for component installation
#
class ganglia::gmetad::install {

  package { 'ganglia-gmetad' :
    ensure => present,
  }
}
