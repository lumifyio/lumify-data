
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

$blurSource   = 'puppet:///modules/blur/apache-blur-0.1.5-bin.tar.gz'
$blurDir      = 'apache-blur-0.1.5'

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

user { 'blur' :
  ensure => 'present',
  gid => 'hadoop',
  home => '/opt/blur-conf',
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

define know-our-host-key ($user, $hostname) {
  exec { "know-our-host-key-${user}-${hostname}" :
    command => "/bin/echo \"${hostname} $(/bin/cat /etc/ssh/ssh_host_rsa_key.pub)\" >> /opt/${user}-conf/.ssh/known_hosts",
    user => "${user}",
    unless => "/bin/grep -q \"${hostname} $(/bin/cat /etc/ssh/ssh_host_rsa_key.pub)\" /opt/${user}-conf/.ssh/known_hosts",
  }
}

define setup-passwordless-ssh ($user = $title) {
  exec { "generate-ssh-keypair-${user}" :
    command => "/usr/bin/ssh-keygen -b 2048 -f /opt/${user}-conf/.ssh/id_rsa -N ''",
    user => "${user}",
    creates => "/opt/${user}-conf/.ssh/id_rsa",
  }

  exec { "authorize-ssh-key-${user}" :
    command => "/bin/cat /opt/${user}-conf/.ssh/id_rsa.pub >> /opt/${user}-conf/.ssh/authorized_keys",
    user => "${user}",
    unless => "/bin/grep -q \"$(/bin/cat /opt/${user}-conf/.ssh/id_rsa.pub)\" /opt/${user}-conf/.ssh/authorized_keys",
    require => Exec["generate-ssh-keypair-${user}"],
  }

  know-our-host-key { "${user}-localhost" :
    user => "${user}",
    hostname => 'localhost',
    require => Exec["generate-ssh-keypair-${user}"],
  }

  know-our-host-key { "${user}-${ipaddress_eth1}" :
    user => "${user}",
    hostname => "${ipaddress_eth1}",
    require => Exec["generate-ssh-keypair-${user}"],
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
    require => [ User["${user}"], Group["${group}"] ],
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

define install ($app = $title, $dirName, $extension, $user = 'hadoop', $group = 'hadoop') {
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

define download-and-install ($app = $title, $dirName, $url, $extension, $user = 'hadoop', $group = 'hadoop') {
  download { "${dirName}" :
    url => "${url}",
    extension => "${extension}",
  }

  install { "${app}" :
    app => "${app}",
    dirName => "${dirName}",
    extension => "${extension}",
    user => "${user}",
    group => "${group}",
    require => Download["${dirName}"],
  }
}

define local-install ($app = $title, $dirName, $source, $extension, $user = 'hadoop', $group = 'hadoop') {
  file { "/opt/downloads/${dirName}.${extension}" :
    source => "${source}",
    require => File['/opt/downloads'],
  }

  install { "${app}" :
    app => "${app}",
    dirName => "${dirName}",
    extension => "${extension}",
    user => "${user}",
    group => "${group}",
    require => File["/opt/downloads/${dirName}.${extension}"],
  }
}

download-and-install { 'hadoop' :
  dirName => "${hadoopDir}",
  url => "${hadoopUrl}",
  extension => 'tar.gz',
}

download-and-install { 'zookeeper' :
  dirName => "${zookeeperDir}",
  url => "${zookeeperUrl}",
  extension => 'tar.gz',
  user => 'zk',
}

download-and-install { 'accumulo' :
  dirName => "${accumuloDir}",
  url => "${accumuloUrl}",
  extension => 'tar.gz',
  user => 'accumulo',
}

download-and-install { 'storm' :
  dirName => "${stormDir}",
  url => "${stormUrl}",
  extension => 'zip',
  user => 'storm',
}

download-and-install { 'tomcat' :
  dirName => "${tomcatDir}",
  url => "${tomcatUrl}",
  extension => 'tar.gz',
  user => 'tomcat',
}

local-install { 'blur' :
  dirName => "${blurDir}",
  source => "${blurSource}",
  extension => 'tar.gz',
  user => 'blur',
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

class { blur::config :
  javaHome => "${javaHome}",
  require => Install['blur'],
}

class { reddawn::config :
  require => [ Install['hadoop'], Install['zookeeper'], Install['storm'], Install['tomcat'], Install['blur'] ],
}

# TODO: configure firewall rules
service { 'iptables' :
  enable => false,
  ensure => 'stopped',
}

