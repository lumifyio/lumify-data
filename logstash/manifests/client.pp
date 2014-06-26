class logstash::client inherits logstash {
  file { '/etc/init.d/logstash-client' :
    ensure  => file,
    mode    => '0744',
    content => template('logstash/logstash-client.erb'),
  }

  service { 'logstash-client' :
    enable  => true,
    ensure  => running,
    require => [
      File['/etc/init.d/logstash-client'],
      File['/opt/logstash'],
    ],
  }
}
