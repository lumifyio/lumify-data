class ganglia::web inherits ganglia {

  package { 'ganglia-web':
    ensure => present,
    require => Package['httpd'],
  }

}
