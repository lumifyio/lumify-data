class hadoop::config::pseudo {
  require hadoop

  package { 'hadoop-0.20-conf-pseudo':
    ensure  => installed,
    require => Package[$hadoop::pkg],
  }
}
