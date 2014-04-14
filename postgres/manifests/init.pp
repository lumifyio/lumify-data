class postgres {
  package { 'postgres-repo':
    source   => 'http://yum.postgresql.org/9.3/redhat/rhel-6-x86_64/pgdg-centos93-9.3-1.noarch.rpm',
    provider => 'rpm',
    ensure   => present,
  }
  
  package { 'postgresql93-server':
    ensure  => present,
    require => Package['postgres-repo'],
  }
  
  package { 'postgresql93':
    ensure  => present,
    require => Package['postgres-repo'],
  }
  
  service { 'postgresql-9.3':
    ensure  => running,
    enable  => true,
    require => Package['postgresql93'],
  }
}