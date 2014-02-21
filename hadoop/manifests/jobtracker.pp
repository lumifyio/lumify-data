class hadoop::jobtracker inherits hadoop {
  package { 'hadoop-0.20-mapreduce-jobtracker':
    ensure  => installed,
    require => Package[$hadoop::pkg],
  }
}
