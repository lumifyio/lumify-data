class hadoop::namenode inherits hadoop {
  package { 'hadoop-0.20-namenode':
    ensure  => installed,
    require => Package['hadoop-0.20'],
  }
}
