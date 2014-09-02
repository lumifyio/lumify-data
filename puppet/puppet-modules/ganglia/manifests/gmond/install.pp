# == Class: ganglia::gmond::install
#
# Responsible for component installation
#
class ganglia::gmond::install {

  package { 'ganglia-gmond' :
    ensure => installed,
  }

  package { 'ganglia-gmond-python' :
    ensure => installed,
  }
}
