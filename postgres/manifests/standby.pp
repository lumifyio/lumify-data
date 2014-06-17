class postgres::standby inherits postgres {
  $replication_enabled = "false"
  $hot_standby = "on"
  $replication_user = hiera("postgres_replication_user","replication")
  $replication_user_pw = hiera("postgres_replication_user_pw")
  $replication_master = hiera("postgres_replication_master")
  $pg_data_dir = hiera("postgres_data_dir","/var/lib/pgsql/9.3/data")
  
<<<<<<< Updated upstream
  pgsql_service { 'postgresql-service':
=======
  postgres::service { 'postgresql-service':
>>>>>>> Stashed changes
    ensure  => 'stopped',
    require => Package['postgresql93'],
  }
  
  file { '/usr/local/sbin/init_streaming_replication':
    ensure  => file,
    content => template('postgres/init_streaming_replication.erb')
  }
  
  setup_configs { "standby_configs":
    require => Service[$serviceName], 
  }
<<<<<<< Updated upstream
}
=======
}
>>>>>>> Stashed changes
