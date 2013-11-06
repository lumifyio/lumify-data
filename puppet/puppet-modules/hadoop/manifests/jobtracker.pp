class hadoop::jobtracker inherits hadoop {
  package { 'hadoop-0.20-jobtracker':
    ensure  => installed,
    require => Package['hadoop-0.20'],
  }
}