class httpd::mod_ssl($httpdVersion='2.2.15', $tmpdir="/usr/local/src") {
	require buildtools
  include macro
  require devel

  package { 'openssl-devel' :
    ensure => installed
  }

  $srctgz = "${tmpdir}/httpd-${httpdVersion}.tar.gz"
  $srcdir = "${tmpdir}/httpd-${httpdVersion}"

  macro::download { "http://archive.apache.org/dist/httpd/httpd-${httpdVersion}.tar.gz" :
    path => $srctgz,
  } -> macro::extract { $srctgz :
    path    => $tmpdir,
    creates => $srcdir,
  }

  $make      = "/usr/bin/make"
  $install   = "/usr/bin/make install"

  exec { 'httpd-configure' :
    cwd     => "${srcdir}",
    command => "${srcdir}/configure --with-apxs=/usr/sbin/apxs --enable-so --enable-ssl",
    creates => "${srcdir}/Makefile",
    require => Macro::Extract[$srctgz],
  }

  exec { 'mod_ssl-make' :
    cwd     => "${srcdir}",
    command => "/usr/bin/make",
    #creates => "${srcdir}/Makefile",
    require => Exec['httpd-configure'],
  }

  /*
  exec { 'mod_ssl-make-install' :
    cwd     => "${srcdir}/modules/ssl",
    command => "/usr/bin/make install",
    #creates => "${srcdir}/Makefile",
    require => Exec['mod_ssl-make'],
  }
  */

  /*
  file { '/etc/httpd/conf.d/mod_jk.conf' :
    ensure  => file,
    source  => "puppet:///modules/httpd/mod_jk.conf",
    owner   => 'root',
    group   => 'root',
    mode    => 'u=rw,go=r',
    require => [File['workers.properties'],File['uriworkermap.properties']],
  }
  */
}
