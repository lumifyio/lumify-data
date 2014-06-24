class logstash_client(
  $version='1.4.2'
){
  $logstash_server = hiera('logstash_server')

  group { 'logstash' :
    ensure => present,
  }

  user { 'logstash' :
    ensure  => present,
    gid     => 'logstash',
    home    => '/opt/logstash',
    require => Group['logstash'],
  }

  macro::download { 'logstash-download':
    url  => "https://download.elasticsearch.org/logstash/logstash/logstash-${version}.tar.gz",
    path => "/tmp/logstash-${version}.tar.gz",
  } -> macro::extract { 'logstash-extract':
    file    => "/tmp/logstash-${version}.tar.gz",
    path    => "/opt",
    creates => "/opt/logstash-${version}",
  }

  file { '/opt/logstash' :
    ensure  => link,
    target  => "/opt/logstash-${version}",
    require => Macro::Extract['logstash-extract'],
  }

  file { '/etc/init.d/logstash' :
    ensure  => file,
    mode => '0744',
    content => template('logstash_client/logstash.erb'),
  }

  file { '/opt/logstash/logstash.config' :
    ensure  => file,
    content => template('logstash_client/logstash.config.erb'),
  }

  service { 'logstash' :
    enable  => true,
    ensure  => running,
    require => [
      File['/etc/init.d/logstash'],
      File['/opt/logstash'],
    ],
  }
}
