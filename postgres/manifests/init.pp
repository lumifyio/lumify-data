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
  
  define postgres::service ($ensure = 'running') {
    service { $serviceName :
      ensure  => $ensure,
      enable  => true,
    }    
  }
  
  define setup_configs () {
    file { '/var/lib/pgsql/9.3/data/pg_hba.conf' :
      ensure  => file,
      content => template('postgres/pg_hba.conf.erb'),
    }

    file { '/var/lib/pgsql/9.3/data/postgresql.conf' :
      ensure  => file,
      content => template('postgres/postgresql.conf.erb'),
    }
  }
}
