class env::cluster::syslog inherits env::cluster::base {
  package { 'rsyslog' :
    ensure => present,
  }
}