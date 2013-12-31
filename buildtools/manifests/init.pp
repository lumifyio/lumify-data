class buildtools($tmpdir="/usr/local/src") {
  $antVersion = '1.9.2'
  $mavenVersion = '3.0.5'

  include epel

  package { 'yasm' :
    ensure => present,
    require => Exec['epel'],
  }

  package { 'git' :
    ensure => present,
  }

  package { 'autoconf':
    ensure => present,
  }

  package { 'automake':
    ensure => present,
  }

  package { 'gcc':
    ensure => present,
  }

  package { 'gcc-c++':
    ensure => present,
  }

  package { 'libtool':
    ensure => present,
  }

  package { 'make':
    ensure => present,
  }

  package { 'cmake' :
    ensure => present,
  }

  package { 'nasm':
    ensure => present,
  }

  package { 'pkgconfig':
    ensure => present,
  }

  package { 'zlib-devel':
    ensure => present,
  }

  package { 'unzip':
    ensure => present,
  }

  macro::download { 'ant-download':
    url  => "http://archive.apache.org/dist/ant/binaries/apache-ant-${antVersion}-bin.tar.gz",
    path => "${tmpdir}/apache-ant-${antVersion}-bin.tar.gz",
  } -> macro::extract { 'ant-extract':
    file    => "${tmpdir}/apache-ant-${antVersion}-bin.tar.gz",
    path    => $tmpdir,
    creates => "${tmpdir}/apache-ant-${antVersion}",
  }

  $ant_home = "${tmpdir}/apache-ant-${antVersion}"

  file { "/etc/profile.d/ant.sh":
    ensure   => file,
    content  => template("buildtools/ant.sh.erb"),
    owner    => "root",
    group    => "root",
    force    => true,
    require  => Macro::Extract['ant-extract'],
  }

  macro::download { 'maven-download':
    url  => "http://archive.apache.org/dist/maven/binaries/apache-maven-${mavenVersion}-bin.tar.gz",
    path => "${tmpdir}/apache-maven-${mavenVersion}-bin.tar.gz",
  } -> macro::extract { 'maven-extract':
    file    => "${tmpdir}/apache-maven-${mavenVersion}-bin.tar.gz",
    path    => $tmpdir,
    creates => "${tmpdir}/apache-maven-${mavenVersion}",
  }

  $mvn_home = "${tmpdir}/apache-maven-${mavenVersion}"

  file { "/etc/profile.d/maven.sh":
    ensure   => file,
    content  => template("buildtools/maven.sh.erb"),
    owner    => "root",
    group    => "root",
    force    => true,
    require  => Macro::Extract['maven-extract'],
  }
}
