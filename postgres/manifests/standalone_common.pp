class postgres::standalone_common inherits postgres {
  exec { 'initdb':
    command => "/sbin/service ${serviceName} initdb",
    onlyif  => "/usr/bin/test ! -f /var/lib/pgsql/9.3/data/PG_VERSION",
    user    => "root",
    group   => "root",
    require => Package['postgresql93'],
  }
  
  exec { "create_db" :
    command => "/usr/bin/psql -c \"create database ${database_name}\"",
    unless  => "/usr/bin/psql -c \"select datname from pg_catalog.pg_database where datname = '${database_name}'\" | grep -q ${database_name}",
    user    => 'postgres',
    require => Postgres::Service['postgresql-service'],
  }  
  
  exec { "create_default_user" :
    command => "/usr/bin/psql -c \"create user ${default_db_user} with password '${default_db_pw}'\"",
    unless  => "/usr/bin/psql -c \"select usename from pg_catalog.pg_user where usename = '${default_db_user}'\" | grep -q ${default_db_user}",
    user    => 'postgres',
    require => Postgres::Service['postgresql-service'],
  }
  
  exec { "grantuser" :
    command => "/usr/bin/psql -c \"grant all privileges on database ${database_name} to ${default_db_user}\"",
    user => 'postgres',
    require => [Exec["create_db"], Exec["create_default_user"]]
  }
  
  postgres::service { 'postgresql-service':
    require => Exec['initdb'],
  }
}