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

  $name_node_ipaddress = "192.168.33.10"
  file { "/etc/hadoop/conf/core-site.xml":
    ensure   => file,
    content  => template("hadoop/core-site.xml.erb"),
    owner    => "root",
    group    => "root",
  }
}
