
exec { 'yum-update' :
  command => '/usr/bin/yum -y update',
  logoutput => 'on_failure',
}

Package {
  provider => 'yum',
  require => Exec['yum-update'],
}

package { 'java-1.6.0-openjdk-devel' :
  ensure => present,
}

package { 'unzip' :
  ensure => present,
}

$hadoopUrl    = 'http://archive.cloudera.com/cdh4/cdh/4/hadoop-2.0.0-cdh4.2.1.tar.gz'
$hadoopDir    = 'hadoop-2.0.0-cdh4.2.1'
$mrUrl        = 'http://archive.cloudera.com/cdh4/cdh/4/mr1-2.0.0-mr1-cdh4.2.1.tar.gz'
$mrDir        = 'hadoop-2.0.0-mr1-cdh4.2.1'
$zookeeperUrl = 'http://archive.cloudera.com/cdh4/cdh/4/zookeeper-3.4.5-cdh4.2.1.tar.gz'
$zookeeperDir = 'zookeeper-3.4.5-cdh4.2.1'

$accumuloUrl  = 'http://www.us.apache.org/dist/accumulo/1.4.3/accumulo-1.4.3-dist.tar.gz'
$accumuloDir  = 'accumulo-1.4.3'

$stormUrl     = 'https://dl.dropbox.com/u/133901206/storm-0.8.2.zip'
$stormDir     = 'storm-0.8.2'

exec { 'download-hadoop' :
  cwd => '/opt',
  command => "/usr/bin/wget ${hadoopUrl} && tar xzf ${hadoopDir}.tar.gz",
  creates => "/opt/${hadoopDir}",
}

exec { 'download-mr' :
  cwd => '/opt',
  command => "/usr/bin/wget ${mrUrl} && tar xzf ${mrDir}.tar.gz",
  creates => "/opt/hadoop-2.0.0-mr1-cdh4.2.1",
}

exec { 'download-zookeeper' :
  cwd => '/opt',
  command => "/usr/bin/wget ${zookeeperUrl} && tar xzf ${zookeeperDir}.tar.gz",
  creates => "/opt/${zookeeperDir}",
}

exec { 'download-accumulo' :
  cwd => '/opt',
  command => "/usr/bin/wget ${accumuloUrl} && tar xzf ${accumuloDir}.tar.gz",
  creates => "/opt/${accumuloDir}",
}

exec { 'download-storm' :
  cwd => '/opt',
  command => "/usr/bin/wget ${stormUrl} && unzip -q ${stormDir}.zip",
  creates => "/opt/${stormDir}",
  require => Package['unzip'],
}

file { '/opt/hadoop':
   ensure => 'link',
   target => "/opt/${hadoopDir}",
   require => Exec['download-hadoop'],
}

file { '/opt/hadoop-mr':
   ensure => 'link',
   target => "/opt/${mrDir}",
   require => Exec['download-mr'],
}

file { '/opt/zookeeper':
   ensure => 'link',
   target => "/opt/${zookeeperDir}",
   require => Exec['download-zookeeper'],
}

file { '/opt/accumulo':
   ensure => 'link',
   target => "/opt/${accumuloDir}",
   require => Exec['download-accumulo'],
}

file { '/opt/storm':
   ensure => 'link',
   target => "/opt/${stormDir}",
   require => Exec['download-storm'],
}

