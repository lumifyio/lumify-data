class postgres::master inherits postgres::standalone {
  $replication_enabled = "true"
  $hot_standby = "off"
  $replication_user = hiera("postgres_replication_user","replication")
  $replication_user_pw = hiera("postgres_replication_user_pw")
      
  exec { "createreplicationuser" :
    command => "/usr/bin/psql -c \"create user ${replication_user} with replication password '${replication_user_pw}'\"",
    unless  => "/usr/bin/psql -c \"select rolname from pg_roles where rolname = '${replication_user}'\" | grep -q ${replication_user}",
    user => 'postgres',
<<<<<<< Updated upstream
    require => Service[$serviceName]
  }
}
=======
    require => Postgres::Service['postgresql-service']
  }
}
>>>>>>> Stashed changes
