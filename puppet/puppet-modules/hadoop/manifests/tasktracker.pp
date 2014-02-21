class hadoop::tasktracker inherits hadoop {
  package { 'hadoop-0.20-mapreduce-tasktracker':
    ensure  => installed,
    require => Package[$hadoop::pkg],
  }
}
