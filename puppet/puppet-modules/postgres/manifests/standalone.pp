class postgres::standalone inherits postgres::standalone_common {
  $replication_enabled = "false"
  $hot_standby = "off"
  $replication_user = ""
  $replication_user_pw = ""
  $replication_master = ""
  
  setup_configs { "standalone_configs":
    require => Exec['initdb'],
    before  => Postgres::Service['postgresql-service'],
  }

}
