
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

$hadoopUrl    = 'http://archive.cloudera.com/cdh/3/hadoop-0.20.2-cdh3u6.tar.gz'
$hadoopDir    = 'hadoop-0.20.2-cdh3u6'
$zookeeperUrl = 'http://archive.cloudera.com/cdh/3/zookeeper-3.3.5-cdh3u4.tar.gz'
$zookeeperDir = 'zookeeper-3.3.5-cdh3u4'

$accumuloUrl  = 'http://www.us.apache.org/dist/accumulo/1.4.3/accumulo-1.4.3-dist.tar.gz'
$accumuloDir  = 'accumulo-1.4.3'

$stormUrl     = 'https://dl.dropbox.com/u/133901206/storm-0.8.2.zip'
$stormDir     = 'storm-0.8.2'

exec { 'download-hadoop' :
  cwd => '/opt',
  command => "/usr/bin/wget ${hadoopUrl} && tar xzf ${hadoopDir}.tar.gz",
  creates => "/opt/${hadoopDir}",
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

exec { 'move-hadoop-conf' :
  cwd => "/opt/${hadoopDir}",
  command => "/bin/mv conf ../hadoop-conf && /bin/ln -s ../hadoop-conf conf",
  creates => '/opt/hadoop-conf',
  require => File['/opt/hadoop'],
}

exec { 'move-zookeeper-conf' :
  cwd => "/opt/${zookeeperDir}",
  command => "/bin/mv conf ../zookeeper-conf && /bin/ln -s ../zookeeper-conf conf",
  creates => '/opt/zookeeper-conf',
  require => File['/opt/zookeeper'],
}

exec { 'move-accumulo-conf' :
  cwd => "/opt/${accumuloDir}",
  command => "/bin/mv conf ../accumulo-conf && /bin/ln -s ../accumulo-conf conf",
  creates => '/opt/accumulo-conf',
  require => File['/opt/accumulo'],
}

exec { 'move-storm-conf' :
  cwd => "/opt/${stormDir}",
  command => "/bin/mv conf ../storm-conf && /bin/ln -s ../storm-conf conf",
  creates => '/opt/storm-conf',
  require => File['/opt/storm'],
}

# TODO:
# move downloads
# config
# start/stop all scripts
# reformat script (HDFS + Accumulo)
