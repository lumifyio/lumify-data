class hadoop {
  include repo::cloudera::cdh3
  require java

  package { 'hadoop-0.20':
    ensure  => installed,
    require => Class['java', 'repo::cloudera::cdh3'],
  }

  package { 'hadoop-0.20-native':
    ensure  => installed,
    require => Package['hadoop-0.20'],
  }
}
