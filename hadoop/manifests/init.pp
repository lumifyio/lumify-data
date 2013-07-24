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

  $namenode_ipaddress = hiera("namenode_ipaddress")

  file { "/etc/hadoop/conf/core-site.xml":
    ensure   => file,
    content  => template("hadoop/core-site.xml.erb"),
    owner    => "root",
    group    => "root",
    force    => true,
    require  => Package['hadoop-0.20'],
  }
}
