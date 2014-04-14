class postgres {
  
  $serviceName = "postgresql-9.3"
    
  package { 'pgdg-centos93-9.3-1':
    source   => 'http://yum.postgresql.org/9.3/redhat/rhel-6-x86_64/pgdg-centos93-9.3-1.noarch.rpm',
    provider => 'rpm',
    ensure   => present,
  }
  
  package { 'postgresql93-server':
    ensure  => present,
    require => Package['pgdg-centos93-9.3-1'],
  }
  
  package { 'postgresql93':
    ensure  => present,
    require => Package['pgdg-centos93-9.3-1'],
  }
  
  exec { 'initdb':
    command => "/sbin/service ${serviceName} initdb",
    onlyif  => "/usr/bin/test ! -d /var/lib/pgsql/9.3/data",
    user    => "root",
    group   => "root",
    require => Package['postgresql93']
  }
  
  service { $serviceName:
    ensure  => running,
    enable  => true,
    require => Exec['initdb'],
  }
}