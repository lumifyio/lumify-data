class hadoop::datanode inherits hadoop {
  package { 'hadoop-0.20-datanode':
    ensure  => installed,
    require => Package['hadoop-0.20'],
  }
}