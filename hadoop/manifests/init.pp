class hadoop {
  include repo::cloudera::cdh3
  require java

  $namenode_ipaddress = hiera("namenode_ipaddress")
  $hadoop_masters = hiera_array('hadoop_masters')
  $hadoop_slaves = hiera_array('hadoop_slaves')

  package { 'hadoop-0.20':
    ensure  => installed,
    require => Class['java', 'repo::cloudera::cdh3'],
  }

  package { 'hadoop-0.20-native':
    ensure  => installed,
    require => Package['hadoop-0.20'],
  }

  file { "/etc/hadoop/conf/core-site.xml":
    ensure   => file,
    content  => template("hadoop/core-site.xml.erb"),
    owner    => "root",
    group    => "root",
    force    => true,
    require  => Package['hadoop-0.20'],
  }

  file { "/etc/hadoop/conf/hdfs-site.xml":
    ensure   => file,
    content  => template("hadoop/hdfs-site.xml.erb"),
    owner    => "root",
    group    => "root",
    force    => true,
    require  => Package['hadoop-0.20'],
  }

  file { "/etc/hadoop/conf/mapred-site.xml":
    ensure   => file,
    content  => template("hadoop/mapred-site.xml.erb"),
    owner    => "root",
    group    => "root",
    force    => true,
    require  => Package['hadoop-0.20'],
  }

  file { "hadoop-masters-config":
    path    => "/etc/hadoop/conf/masters",
    ensure  => file,
    content => template("hadoop/masters.erb"),
    require => Package['hadoop-0.20'],
  }

  file { "hadoop-slaves-config":
    path    => "/etc/hadoop/conf/slaves",
    ensure  => file,
    content => template("hadoop/slaves.erb"),
    require => Package['hadoop-0.20'],
  }

  file { "/usr/lib/hadoop/.ssh" :
    ensure  => directory,
    owner   => 'hdfs',
    group   => 'hadoop',
    mode    => 'u=rwx,go=',
    require => Package['hadoop-0.20'],
  }

  macro::setup-passwordless-ssh { 'hdfs' :
    sshdir  => '/usr/lib/hadoop/.ssh',
    require => File['/usr/lib/hadoop/.ssh'],
  }

  file { '/data0' :
    ensure  => directory,
  }

  file { [ '/data0/hadoop', '/data0/hadoop/tmp' ] :
    ensure  => directory,
    owner   => 'hdfs',
    group   => 'hadoop'
    mode    => 'u=rwx,g=rx,o=',
    require => File['/data0'],
  }

  file { [ '/data0/hdfs', '/data0/hdfs/name', '/data0/hdfs/data' ] :
    ensure  => directory,
    owner   => 'hdfs',
    group   => 'hadoop'
    mode    => 'u=rwx,g=rx,o=',
    require => File['/data0'],
  }

  file { [ '/data0/mapred', '/data0/mapred/local' ] :
    ensure  => directory,
    owner   => 'mapred',
    group   => 'hadoop'
    mode    => 'u=rwx,g=rx,o=',
    require => File['/data0'],
  }
}
