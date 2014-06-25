class logstash(
  $version='1.4.2'
){
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
}
