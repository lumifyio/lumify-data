class hadoop::config::pseudo {
  require hadoop

  package { 'hadoop-conf-pseudo':
    ensure  => installed,
    require => Package[$hadoop::pkg],
  }
}
