
$javaPackage  = 'java-1.6.0-openjdk-devel'
$javaHome     = '/usr/lib/jvm/java-1.6.0-openjdk.x86_64'

$hadoopUrl    = 'http://archive.cloudera.com/cdh/3/hadoop-0.20.2-cdh3u6.tar.gz'
$hadoopDir    = 'hadoop-0.20.2-cdh3u6'
$zookeeperUrl = 'http://archive.cloudera.com/cdh/3/zookeeper-3.3.5-cdh3u4.tar.gz'
$zookeeperDir = 'zookeeper-3.3.5-cdh3u4'

$accumuloUrl  = 'http://www.us.apache.org/dist/accumulo/1.4.3/accumulo-1.4.3-dist.tar.gz'
$accumuloDir  = 'accumulo-1.4.3'

$stormUrl     = 'https://dl.dropbox.com/u/133901206/storm-0.8.2.zip'
$stormDir     = 'storm-0.8.2'

$tomcatUrl    = 'http://www.us.apache.org/dist/tomcat/tomcat-7/v7.0.40/bin/apache-tomcat-7.0.40.tar.gz'
$tomcatDir    = 'apache-tomcat-7.0.40'

exec { 'yum-update' :
  command => '/usr/bin/yum -y update',
  logoutput => 'on_failure',
}

Package {
  provider => 'yum',
  require => Exec['yum-update'],
}

package { "${javaPackage}" :
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

user { 'tomcat' :
  ensure => 'present',
  gid => 'hadoop',
  home => '/opt/tomcat-conf',
}

file { '/opt/downloads' :
  ensure => 'directory',
}

define find-and-replace ($file, $find, $replace) {
  exec { "find-and-replace-${title}" :
    command => "/bin/sed -i.DIST -e 's|${find}|${replace}|' ${file}",
    unless => "/bin/grep -q '${replace}' ${file}",
  }
}

define download ($dirName = $title, $url, $extension) {
  exec { "download-${dirName}" :
    cwd => '/opt',
    command => "/usr/bin/curl ${url} -s -L -o downloads/${dirName}.${extension}",
    creates => "/opt/downloads/${dirName}.${extension}",
    require => File['/opt/downloads'],
  }
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
    require => [ Download["${dirName}"], User["${user}"], Group["${group}"] ],
  }
}

define relocate-conf ($app = $title) {
  exec { "relocate-conf-${app}" :
    cwd => "/opt/${app}",
    command => "/bin/mv conf ../${app}-conf && /bin/ln -s ../${app}-conf conf",
    unless => "/bin/readlink conf | /bin/grep -q '../${app}-conf'",
    require => File["/opt/${app}"],
  }
}

define install ($app = $title, $dirName, $url, $extension, $user = 'hadoop', $group = 'hadoop') {
  download { "${dirName}" :
    url => "${url}",
    extension => "${extension}",
  }

  extract { "${dirName}" :
    extension => "${extension}",
    user => "${user}",
    group => "${group}",
  }

  file { "/opt/${app}":
    ensure => 'link',
    target => "/opt/${dirName}",
    require => Extract["${dirName}"],
  }

  relocate-conf { "${app}" :
  }
}

install { 'hadoop' :
  dirName => "${hadoopDir}",
  url => "${hadoopUrl}",
  extension => 'tar.gz',
}

install { 'zookeeper' :
  dirName => "${zookeeperDir}",
  url => "${zookeeperUrl}",
  extension => 'tar.gz',
  user => 'zk',
}

install { 'accumulo' :
  dirName => "${accumuloDir}",
  url => "${accumuloUrl}",
  extension => 'tar.gz',
  user => 'accumulo',
}

install { 'storm' :
  dirName => "${stormDir}",
  url => "${stormUrl}",
  extension => 'zip',
  user => 'storm',
}

install { 'tomcat' :
  dirName => "${tomcatDir}",
  url => "${tomcatUrl}",
  extension => 'tar.gz',
  user => 'tomcat',
}

class { hadoop::config :
  javaHome => "${javaHome}",
  require => Install['hadoop'],
}

class { zookeeper::config :
  require => Install['zookeeper'],
}

class { accumulo::config :
  javaHome => "${javaHome}",
  require => Install['accumulo'],
}

#class { storm::config :
#  require => Install['storm'],
#}

#class { tomcat::config :
#  require => Install['tomcat'],
#}

class { reddawn::config :
  require => [ Install['hadoop'], Install['zookeeper'], Install['storm'], Install['tomcat'] ],
}

# TODO:
# config
# start/stop all scripts
# reformat script (HDFS + Accumulo)

