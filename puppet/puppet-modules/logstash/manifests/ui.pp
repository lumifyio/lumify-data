class logstash::ui inherits logstash {
  file { '/etc/init.d/logstash-ui' :
    ensure  => file,
    mode    => '0744',
    content => template('logstash/logstash-ui.erb'),
  }

  service { 'logstash-ui' :
    enable  => true,
    ensure  => running,
    require => [
      File['/etc/init.d/logstash-ui'],
      File['/opt/logstash'],
    ],
  }
}
