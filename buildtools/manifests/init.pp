class buildtools($tmpdir="/usr/local/src") {
  $antVersion = '1.9.2'
  $mavenVersion = '3.0.5'

  exec { 'epel':
    command => '/bin/rpm -ivH http://dl.fedoraproject.org/pub/epel/6/x86_64/epel-release-6-8.noarch.rpm',
    unless => '/bin/rpm -q epel-release-6-8',
  }

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

  macro::download { 'ant-download':
    url  => "http://www.us.apache.org/dist/ant/binaries/apache-ant-${antVersion}-bin.tar.gz",
    path => "${tmpdir}/apache-ant-${antVersion}-bin.tar.gz",
  } -> macro::extract { 'ant-extract':
    file => "${tmpdir}/apache-ant-${antVersion}-bin.tar.gz",
    path => $tmpdir,
  }

  macro::download { 'maven-download':
    url  => "http://www.us.apache.org/dist/maven/binaries/apache-maven-${mavenVersion}-bin.tar.gz",
    path => "${tmpdir}/apache-maven-${mavenVersion}-bin.tar.gz",
  } -> macro::extract { 'maven-extract':
    file => "${tmpdir}/apache-maven-${mavenVersion}-bin.tar.gz",
    path => $tmpdir,
  }
}
