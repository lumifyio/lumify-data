class env::cluster::loggly {
  $loggly_token = hiera('loggly_token')
  $loggly_tag = hiera('loggly_tag')
  $facility = 'local3' # use '*' to send everything

  file { '/etc/rsyslog.d/22-loggly.conf' :
    owner   => 'root',
    group   => 'root',
    mode    => '0644',
    content => template('env/cluster/22-loggly.conf.erb'),
    require => Package['rsyslog'],
    notify  => Exec['restart_rsyslogd'],
  }
}
