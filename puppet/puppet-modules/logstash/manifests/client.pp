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

  define group_membership($group) {
    ensure_resource('exec', "add-logstash-to-group-${group}", {
      'command' => "/usr/sbin/usermod -a -G ${group} logstash",
      'unless'  => "/usr/bin/groups logstash | /bin/grep -q ${group}",
      'returns' => [ 0, 6 ],
      'require' => User['logstash']
    })
  }
}
