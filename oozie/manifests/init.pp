class oozie {
  package { 'oozie':
    ensure  => installed,
    require => Package['hadoop-0.20'],
  }
}