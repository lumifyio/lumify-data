class hadoop::datanode inherits hadoop {
  package { 'hadoop-hdfs-datanode':
    ensure  => installed,
    require => Package['hadoop.x86_64'],
  }
}