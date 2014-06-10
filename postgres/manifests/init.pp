class postgres {
  
  $serviceName = "postgresql-9.3"
  $connect_network = hiera("postgres_connect_network")
  $listen_addresses = hiera_array("postgres_listen_addresses")
  $listen_port = hiera("postgres_listen_port",5432)
  $max_connections = hiera("postgres_max_connections",100)
  $database_name = hiera("postgres_db_name")
  $default_db_user = hiera("postgres_db_user")
  $default_db_pw = hiera("postgres_db_pw")
    
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
    onlyif  => "/usr/bin/test ! -f /var/lib/pgsql/9.3/data/PG_VERSION",
    user    => "root",
    group   => "root",
    require => Package['postgresql93']
  }
  
  file { '/var/lib/pgsql/9.3/data/pg_hba.conf' :
    ensure  => file,
    content => template('postgres/pg_hba.conf.erb'),
    require => Exec['initdb'],
    before  => Service[$serviceName],
  }
  
  file { '/var/lib/pgsql/9.3/data/postgresql.conf' :
    ensure  => file,
    content => template('postgres/postgresql.conf.erb'),
    require => Exec['initdb'],
    before  => Service[$serviceName],
  }
  
  service { $serviceName:
    ensure  => running,
    enable  => true,
    require => Exec['initdb'],
  }
  
  exec { "createdb" :
    command => "/usr/bin/psql -c \"create database ${database_name}\"",
    unless  => "/usr/bin/psql -c \"select datname from pg_catalog.pg_database where datname = '${database_name}'\" | grep -q ${database_name}",
    user    => 'postgres',
    require => Service[$serviceName]
  }  
  
  exec { "createuser" :
    command => "/usr/bin/psql -c \"create user ${default_db_user} with password '${default_db_pw}'\"",
    unless  => "/usr/bin/psql -c \"select usename from pg_catalog.pg_user where usename = '${default_db_user}'\" | grep -q ${default_db_user}",
    user => 'postgres',
    require => Service[$serviceName]
  }
  
  
  exec { "grantuser" :
    command => "/usr/bin/psql -c \"grant all privileges on database ${database_name} to ${default_db_user}\"",
    user => 'postgres',
    require => [Exec["createdb"], Exec["createuser"]]
  }
}