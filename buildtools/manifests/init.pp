class buildtools($tmpdir="/usr/local/src") {
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

  macro::download { "ant-download":
    url  => "http://apache.mirrors.hoobly.com//ant/binaries/apache-ant-1.9.2-bin.tar.gz",
    path => "${tmpdir}/apache-ant-1.9.2-bin.tar.gz",
  } -> macro::extract { 'extract-ant':
    file => "${tmpdir}/apache-ant-1.9.2-bin.tar.gz",
    path => $tmpdir,
  }

}