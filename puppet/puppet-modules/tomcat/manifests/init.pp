class tomcat(
  $version = "7.0.52",
  $user = "tomcat",
  $group = "tomcat",
  $installdir = "/opt",
  $home = "/opt/tomcat",
  $tmpdir = '/tmp',
) {
  include macro
  require java

  $extractdir = "${installdir}/apache-tomcat-${version}"

  group { $group :
    ensure => present,
  }

  user { $user :
    ensure  => present,
    gid     => $group,
    home    => $home,
    require => Group[$group],
  }

  macro::download { 'tomcat-download':
    url  => "http://archive.apache.org/dist/tomcat/tomcat-7/v${version}/bin/apache-tomcat-${version}.tar.gz",
    path => "${tmpdir}/apache-tomcat-${version}.tar.gz",
  } -> macro::extract { 'extract-tomcat':
    file    => "${tmpdir}/apache-tomcat-${version}.tar.gz",
    path    => $installdir,
    creates => $extractdir,
  }

  file { $extractdir:
    ensure  => directory,
    owner   => $user,
    group   => $group,
    recurse => true,
    require => Macro::Extract['extract-tomcat'],
  }

  file { $home:
    ensure  => link,
    target  => $extractdir,
    require => File[$extractdir],
  }

  $tomcat_java_opts = hiera('tomcat_java_opts','')

  file { '/etc/init/tomcat.conf':
    ensure  => file,
    content => template('tomcat/upstart.conf.erb'),
    require => File[$home],
  }
}
