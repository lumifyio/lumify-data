
$hadoopUrl    = 'http://archive.cloudera.com/cdh/3/hadoop-0.20.2-cdh3u6.tar.gz'
$hadoopDir    = 'hadoop-0.20.2-cdh3u6'
$zookeeperUrl = 'http://archive.cloudera.com/cdh/3/zookeeper-3.3.5-cdh3u4.tar.gz'
$zookeeperDir = 'zookeeper-3.3.5-cdh3u4'

$accumuloUrl  = 'http://www.us.apache.org/dist/accumulo/1.4.3/accumulo-1.4.3-dist.tar.gz'
$accumuloDir  = 'accumulo-1.4.3'

$stormUrl     = 'https://dl.dropbox.com/u/133901206/storm-0.8.2.zip'
$stormDir     = 'storm-0.8.2'

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

group { 'hadoop' :
  ensure => 'present',
}

user { 'hadoop' :
  ensure => 'present',
  gid => 'hadoop',
  home => '/opt/hadoop-conf',
}

user { 'hdfs' :
  ensure => 'present',
  gid => 'hadoop',
  home => '/opt/hadoop-conf',
}

user { 'mapred' :
  ensure => 'present',
  gid => 'hadoop',
  home => '/opt/hadoop-conf',
}

user { 'zk' :
  ensure => 'present',
  gid => 'hadoop',
  home => '/opt/zookeeper-conf',
}

user { 'accumulo' :
  ensure => 'present',
  gid => 'hadoop',
  home => '/opt/accumulo-conf',
}

user { 'storm' :
  ensure => 'present',
  gid => 'hadoop',
  home => '/opt/storm-conf',
}

file { '/opt/downloads' :
  ensure => 'directory',
}

define download ($dirName = $title, $url, $extension) {
  exec { "download-${dirName}" :
    cwd => '/opt',
    command => "/usr/bin/curl ${url} -s -L -o downloads/${dirName}.${extension}",
    creates => "/opt/downloads/${dirName}.${extension}",
    require => File['/opt/downloads'],
  }
}

download { "${hadoopDir}" :
  url => "${hadoopUrl}",
  extension => 'tar.gz',
}

download { "${zookeeperDir}" :
  url => "${zookeeperUrl}",
  extension => 'tar.gz',
}

download { "${accumuloDir}" :
  url => "${accumuloUrl}",
  extension => 'tar.gz',
}

download { "${stormDir}" :
  url => "${stormUrl}",
  extension => 'zip',
}

define extract ($dirName = $title, $extension, $user = 'hadoop', $group = 'hadoop') {
  case $extension {
    'zip':   { $cmd = '/usr/bin/unzip -q' }
    default: { $cmd = '/bin/tar xzf' }
  }

  exec { "extract-${dirName}" :
    cwd => '/opt',
    command => "${cmd} downloads/${dirName}.${extension} && /bin/chown -R ${user}:${group} /opt/${dirName}",
    creates => "/opt/${dirName}",
    require => [ Download["${dirName}"], User["${user}"] ],
  }
}

extract { "${hadoopDir}" :
  extension => 'tar.gz',
}

extract { "${zookeeperDir}" :
  extension => 'tar.gz',
  user => 'zk',
}

extract { "${accumuloDir}" :
  extension => 'tar.gz',
  user => 'accumulo',
}

extract { "${stormDir}" :
  extension => 'zip',
  user => 'storm',
}

file { '/opt/hadoop':
  ensure => 'link',
  target => "/opt/${hadoopDir}",
  require => Extract["${hadoopDir}"],
}

file { '/opt/zookeeper':
  ensure => 'link',
  target => "/opt/${zookeeperDir}",
  require => Extract["${zookeeperDir}"],
}

file { '/opt/accumulo':
  ensure => 'link',
  target => "/opt/${accumuloDir}",
  require => Extract["${accumuloDir}"],
}

file { '/opt/storm':
  ensure => 'link',
  target => "/opt/${stormDir}",
  require => Extract["${stormDir}"],
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
# config
# start/stop all scripts
# reformat script (HDFS + Accumulo)

