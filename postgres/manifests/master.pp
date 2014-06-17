class postgres::master inherits postgres::standalone_common {
  $replication_enabled = "true"
  $hot_standby = "off"
  $replication_user = hiera("postgres_replication_user","replication")
  $replication_user_pw = hiera("postgres_replication_user_pw")
  $replication_max_senders = hiera("postgres_replication_max_senders", 5)
  $replication_keep_segments = hiera("postgres_replication_keep_segments", 32)
  
  setup_configs { "master_configs":
    require => Exec['initdb'],
    before  => Postgres::Service['postgresql-service'],
  }
      
  exec { "createreplicationuser" :
    command => "/usr/bin/psql -c \"create user ${replication_user} with replication password '${replication_user_pw}'\"",
    unless  => "/usr/bin/psql -c \"select rolname from pg_roles where rolname = '${replication_user}'\" | grep -q ${replication_user}",
    user => 'postgres',
    require => Postgres::Service['postgresql-service']
  }
}
