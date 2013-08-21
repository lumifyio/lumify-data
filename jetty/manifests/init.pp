class jetty(
  $major_version='8',
  $version='8.1.12.v20130726'
){
  require java

  group { 'jetty' :
    ensure => present,
  }

  user { 'jetty' :
   ensure  => present,
   gid     => 'jetty',
   home    => '/opt/jetty',
   require => Group['jetty'],
  }

  macro::download { 'jetty-download':
    url  => "http://eclipse.org/downloads/download.php?file=/jetty/stable-${major_version}/dist/jetty-distribution-${version}.tar.gz&r=1",
    path => "/tmp/jetty-distribution-${version}.tar.gz",
  } -> macro::extract { 'jetty-extract':
    file    => "/tmp/jetty-distribution-${version}.tar.gz",
    path    => "/opt",
    creates => "/opt/jetty-distribution-${version}",
  }

  file { "/opt/jetty-distribution-${version}" :
    ensure  => directory,
    owner   => 'jetty',
    group   => 'jetty',
    require => [ User['jetty'], Macro::Extract['jetty-extract'] ],
  }

  file { '/opt/jetty' :
    ensure  => link,
    target  => "/opt/jetty-distribution-${version}",
    require => File["/opt/jetty-distribution-${version}"],
  }

  file { '/etc/init.d/jetty' :
    ensure  => link,
    target  => '/opt/jetty/bin/jetty.sh',
    require => File['/opt/jetty'],
  }

  file { '/opt/jetty/contexts-DISABLED' :
    ensure  => directory,
    require => File['/opt/jetty'],
  }

  file { '/opt/jetty/webapps-DISABLED' :
    ensure  => directory,
    require => File['/opt/jetty'],
  }

  exec { 'jetty-disable-contexts' :
    command => '/bin/mv /opt/jetty/contexts/* /opt/jetty/contexts-DISABLED',
    unless  => '/usr/bin/test -f /opt/jetty/contexts-DISABLED/test.xml',
    require => File['/opt/jetty/contexts-DISABLED'],
  }

  exec { 'jetty-disable-webapps' :
    command => '/bin/mv /opt/jetty/webapps/* /opt/jetty/webapps-DISABLED',
    unless  => '/usr/bin/test -f /opt/jetty/webapps-DISABLED/test.war',
    require => File['/opt/jetty/webapps-DISABLED'],
  }

  service { 'jetty' :
    enable  => true,
    ensure  => running,
    require => [ File['/etc/init.d/jetty'], Exec['jetty-disable-contexts'], Exec['jetty-disable-webapps'] ],
  }

  exec { 'jetty-deploy-wars' :
    command => '/bin/mv -f /root/*.war /opt/jetty/webapps',
    onlyif  => '/bin/ls /root/*.war',
    require => Service['jetty'],
  }
}
