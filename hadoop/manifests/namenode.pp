class hadoop::namenode inherits hadoop {
  package { 'hadoop-hdfs-namenode':
    ensure  => installed,
    require => Package['hadoop.x86_64'],
  }
}
