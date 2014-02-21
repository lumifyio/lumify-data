class hadoop::secondarynamenode inherits hadoop {
  package { 'hadoop-hdfs-secondarynamenode':
    ensure  => installed,
    require => Package[$hadoop::pkg],
  }
}
