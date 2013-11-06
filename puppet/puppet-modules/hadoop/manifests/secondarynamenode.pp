class hadoop::secondarynamenode inherits hadoop {
  package { 'hadoop-0.20-secondarynamenode':
    ensure  => installed,
    require => Package['hadoop-0.20'],
  }
}
