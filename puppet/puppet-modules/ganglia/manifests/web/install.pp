# == Class: ganglia::web::install
#
# Responsible for component installation
#
class ganglia::web::install {

  package { 'ganglia-web' :
    ensure => present,
  }
}
