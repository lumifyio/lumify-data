class hadoop::tasktracker inherits hadoop {
  package { 'hadoop-0.20-tasktracker':
    ensure  => installed,
    require => Package['hadoop-0.20'],
  }
}