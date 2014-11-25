class cloudera::cdh5::hadoop::base {
  include cloudera::cdh5::repo
  require java

  case $architecture {
    'x86_64': { $pkg = 'hadoop.x86_64' }
    'i386':   { $pkg = 'hadoop.i686' }
    default:  { fail "unsupported architecture: ${architecture}" }
  }
  $namenode_ipaddress = hiera("namenode_ipaddress")
  $namenode_rpc_address = hiera("namenode_rpc_address","0.0.0.0:8020")
  $namenode_rpc_bind_host = hiera("namenode_rpc_bind_host","0.0.0.0")
  $namenode_hostname = hiera("namenode_hostname")
  $datanode_address = hiera("datanode_address","0.0.0.0:50010")
  $datanode_ipc_address = hiera("datanode_ipc_address","0.0.0.0:50020")
  $hadoop_masters = hiera_array('hadoop_masters')
  $hadoop_slaves = hiera_array('hadoop_slaves')
  $historyserver_hostname = hiera("historyserver_hostname")

  $hadoop_ha_enabled = hiera("hadoop_ha_enabled","false")

  if ($hadoop_ha_enabled == true) {
    $hadoop_ha_cluster_name = hiera("hadoop_ha_cluster_name")
    $hadoop_namenodes = hiera_array("hadoop_namenodes")
    $hadoop_namenode_rpc_port = hiera("hadoop_namenode_rpc_port","8020")
    $hadoop_namenode_http_port = hiera("hadoop_namenode_http_port","50070")
    $hadoop_ha_journalnodes = hiera_array("hadoop_ha_journalnodes")
    $hadoop_ha_journalnode_edits_dir = hiera("hadoop_ha_journalnode_edits_dir")
    $zookeeper_nodes = hiera("zookeeper_nodes")
  }

  group { 'hadoop' :
    ensure => present,
  }

  user { [ 'hdfs', 'mapred' ] :
    ensure => present,
    gid => 'hadoop',
    require => Group['hadoop'],
  }

  group { 'yarn' :
    ensure => present,
  }

  user { 'yarn' :
    ensure => present,
    gid => 'yarn',
    require => Group['yarn'],
  }

  package { $pkg :
    ensure  => installed,
    require => Class['java', 'cloudera::cdh5::repo'],
  }

  file { "/etc/hadoop/conf/core-site.xml":
    ensure  => file,
    content => template("cloudera/core-site.xml.erb"),
    owner   => "root",
    group   => "root",
    mode    => "u=rw,go=r",
    require => [ Package[$pkg], File['/data0/hadoop'] ],
  }

  file { "/etc/hadoop/conf/hdfs-site.xml":
    ensure  => file,
    content => template("cloudera/hdfs-site.xml.erb"),
    owner   => "root",
    group   => "root",
    mode    => "u=rw,go=r",
    require => [ Package[$pkg], File['/data0/hdfs'] ],
  }

  file { "/etc/hadoop/conf/mapred-site.xml":
    ensure  => file,
    source  => "puppet:///modules/cloudera/mapred-site.xml",
    owner   => "root",
    group   => "root",
    mode    => "u=rw,go=r",
    require => [ Package[$pkg], File['/data0/yarn'] ],
  }

  file { "/etc/hadoop/conf/log4j.properties":
    ensure  => file,
    source  => "puppet:///modules/cloudera/log4j.properties",
    owner   => "root",
    group   => "root",
    mode    => "u=rw,go=r",
    require => Package[$pkg],
  }

  file { "/etc/hadoop/conf/masters":
    ensure  => file,
    content => template("cloudera/masters.erb"),
    owner   => "root",
    group   => "root",
    mode    => "u=rw,go=r",
    require => Package[$pkg],
  }

  file { "/etc/hadoop/conf/slaves":
    ensure  => file,
    content => template("cloudera/slaves.erb"),
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
    ensure_resource('file', "${name}", {ensure => directory})

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

    file { [ "${name}/yarn", "${name}/yarn/local", "${name}/yarn/logs" ] :
      ensure  => directory,
      owner   => 'yarn',
      group   => 'yarn',
      mode    => 'u=rwx,g=rx,o=',
      require =>  [ File["${name}"], Package[$pkg], User['yarn'] ],
    }
  }

  $data_dir_list = split($data_directories, ',')

  setup_data_directory { $data_dir_list :
    pkg => $pkg,
  }
}
