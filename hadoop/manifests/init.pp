class hadoop {
  include repo::cloudera::cdh4
  require java

  case $architecture {
    'x86_64': { $pkg = 'hadoop.x86_64' }
    'i386':   { $pkg = 'hadoop.i686' }
    default:  { fail "unsupported architecture: ${architecture}" }
  }
  $namenode_ipaddress = hiera("namenode_ipaddress")
  $namenode_hostname = hiera("namenode_hostname")
  $hadoop_masters = hiera_array('hadoop_masters')
  $hadoop_slaves = hiera_array('hadoop_slaves')

  group { 'hadoop' :
    ensure => present,
  }

  user { [ 'hdfs', 'mapred' ] :
    ensure => present,
    gid => 'hadoop',
    require => Group['hadoop'],
  }

  package { $pkg :
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
    ensure  => file,
    content => template("hadoop/core-site.xml.erb"),
    owner   => "root",
    group   => "root",
    mode    => "u=rw,go=r",
    require => [ Package[$pkg], File['/data0/hadoop'] ],
  }

  file { "/etc/hadoop/conf/hdfs-site.xml":
    ensure  => file,
    content => template("hadoop/hdfs-site.xml.erb"),
    owner   => "root",
    group   => "root",
    mode    => "u=rw,go=r",
    require => [ Package[$pkg], File['/data0/hdfs'] ],
  }

  file { "/etc/hadoop/conf/mapred-site.xml":
    ensure  => file,
    content => template("hadoop/mapred-site.xml.erb"),
    owner   => "root",
    group   => "root",
    mode    => "u=rw,go=r",
    require => [ Package[$pkg], File['/data0/mapred'] ],
  }

  file { "/etc/hadoop/conf/log4j.properties":
    ensure  => file,
    source  => "puppet:///modules/hadoop/log4j.properties",
    owner   => "root",
    group   => "root",
    mode    => "u=rw,go=r",
    require => Package[$pkg],
  }

  file { "/etc/hadoop/conf/masters":
    ensure  => file,
    content => template("hadoop/masters.erb"),
    owner   => "root",
    group   => "root",
    mode    => "u=rw,go=r",
    require => Package[$pkg],
  }

  file { "/etc/hadoop/conf/slaves":
    ensure  => file,
    content => template("hadoop/slaves.erb"),
    owner   => "root",
    group   => "root",
    mode    => "u=rw,go=r",
    require => Package[$pkg],
  }

  file { "/usr/lib/hadoop/.ssh" :
    ensure  => directory,
    owner   => 'hdfs',
    group   => 'hadoop',
    mode    => 'u=rwx,go=',
    require => Package[$pkg],
  }

  macro::setup-passwordless-ssh { 'hdfs' :
    sshdir  => '/usr/lib/hadoop/.ssh',
    require => File['/usr/lib/hadoop/.ssh'],
  }

  define setup_data_directory ($pkg) {
    file { "${name}" :
      ensure  => directory,
    }

    file { [ "${name}/hadoop", "${name}/hadoop/tmp" ] :
      ensure  => directory,
      owner   => 'hdfs',
      group   => 'hadoop',
      mode    => 'u=rwx,g=rwx,o=',
      require =>  [ File["${name}"], Package[$pkg], User['hdfs'] ],
    }

    file { [ "${name}/hdfs", "${name}/hdfs/name", "${name}/hdfs/data" ] :
      ensure  => directory,
      owner   => 'hdfs',
      group   => 'hadoop',
      mode    => 'u=rwx,g=rx,o=',
      require =>  [ File["${name}"], Package[$pkg], User['hdfs'] ],
    }

    file { [ "${name}/mapred", "${name}/mapred/local" ] :
      ensure  => directory,
      owner   => 'mapred',
      group   => 'hadoop',
      mode    => 'u=rwx,g=rx,o=',
      require =>  [ File["${name}"], Package[$pkg], User['mapred'] ],
    }
  }

  $data_dir_list = split($data_directories, ',')

  setup_data_directory { $data_dir_list :
    pkg => $pkg,
  }
}
