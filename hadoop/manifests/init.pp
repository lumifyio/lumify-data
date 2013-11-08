class hadoop {
  include repo::cloudera::cdh4
  require java

  $namenode_ipaddress = hiera("namenode_ipaddress")
  $hadoop_masters = hiera_array('hadoop_masters')
  $hadoop_slaves = hiera_array('hadoop_slaves')

  package { 'hadoop.x86_64':
    ensure  => installed,
    require => Class['java', 'repo::cloudera::cdh4'],
  }

/*
  package { 'hadoop-0.20-native':
    ensure  => installed,
    require => Package['hadoop-0.20'],
  }
*/

  file { "/etc/hadoop/conf/core-site.xml":
    ensure   => file,
    content  => template("hadoop/core-site.xml.erb"),
    owner    => "root",
    group    => "root",
    force    => true,
    require  => [ Package['hadoop.x86_64'], File['/data0/hadoop'] ],
  }

  file { "/etc/hadoop/conf/hdfs-site.xml":
    ensure   => file,
    content  => template("hadoop/hdfs-site.xml.erb"),
    owner    => "root",
    group    => "root",
    force    => true,
    require  => [ Package['hadoop.x86_64'], File['/data0/hdfs'] ],
  }

  file { "/etc/hadoop/conf/mapred-site.xml":
    ensure   => file,
    content  => template("hadoop/mapred-site.xml.erb"),
    owner    => "root",
    group    => "root",
    force    => true,
    require  => [ Package['hadoop.x86_64'], File['/data0/mapred'] ],
  }

  file { "hadoop-masters-config":
    path    => "/etc/hadoop/conf/masters",
    ensure  => file,
    content => template("hadoop/masters.erb"),
    require => Package['hadoop.x86_64'],
  }

  file { "hadoop-slaves-config":
    path    => "/etc/hadoop/conf/slaves",
    ensure  => file,
    content => template("hadoop/slaves.erb"),
    require => Package['hadoop.x86_64'],
  }

  file { "/usr/lib/hadoop/.ssh" :
    ensure  => directory,
    owner   => 'hdfs',
    group   => 'hadoop',
    mode    => 'u=rwx,go=',
    require => Package['hadoop.x86_64'],
  }

  macro::setup-passwordless-ssh { 'hdfs' :
    sshdir  => '/usr/lib/hadoop/.ssh',
    require => File['/usr/lib/hadoop/.ssh'],
  }

  define setup_data_directory {
    file { "${name}" :
      ensure  => directory,
    }

    file { [ "${name}/hadoop", "${name}/hadoop/tmp" ] :
      ensure  => directory,
      owner   => 'hdfs',
      group   => 'hadoop',
      mode    => 'u=rwx,g=rwx,o=',
      require =>  [ File["${name}"], Package['hadoop.x86_64'] ],
    }

    file { [ "${name}/hdfs", "${name}/hdfs/name", "${name}/hdfs/data" ] :
      ensure  => directory,
      owner   => 'hdfs',
      group   => 'hadoop',
      mode    => 'u=rwx,g=rx,o=',
      require =>  [ File["${name}"], Package['hadoop.x86_64'] ],
    }

    file { [ "${name}/mapred", "${name}/mapred/local" ] :
      ensure  => directory,
      owner   => 'mapred',
      group   => 'hadoop',
      mode    => 'u=rwx,g=rx,o=',
      require =>  [ File["${name}"], Package['hadoop.x86_64'] ],
    }
  }

  $data_dir_list = split($data_directories, ',')

  setup_data_directory { $data_dir_list : }
}
