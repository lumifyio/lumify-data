class httpd::mod_ssl($httpdVersion='2.2.15', $tmpdir="/usr/local/src", $clientAuth="require") {

  define build_mod_ssl (
    $httpdVersion = $title,
    $tmpdir
  ) {
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

    exec { 'httpd-configure' :
      cwd     => "${srcdir}",
      command => "${srcdir}/configure --with-apxs=/usr/sbin/apxs --enable-ssl=shared",
      creates => "${srcdir}/Makefile",
      require => Macro::Extract[$srctgz],
    }

    exec { 'httpd-make' :
      cwd     => "${srcdir}",
      command => "/usr/bin/make",
      creates => "${srcdir}/modules/ssl/.libs/mod_ssl.so",
      require => Exec['httpd-configure'],
    }

    file { '/etc/httpd/modules/mod_ssl.so' :
      ensure  => file,
      source  => "file://${srcdir}/modules/ssl/.libs/mod_ssl.so",
      require => Exec['httpd-make'],
    }
  }

  if $httpdVersion == '2.4.9-1' { # specal handling for our handmade RPM
    package { 'mod_ssl' :
      ensure => '2.4.9-1',
    }
  } else {
    build_mod_ssl { $httpdVersion :
      tmpdir => $tmpdir,
    }
  }

  $httpd_ssl_listen_port = hiera('httpd_ssl_listen_port')
  $httpd_ssl_certificate_file = hiera('httpd_ssl_certificate_file')
  $httpd_ssl_certificate_key_file = hiera('httpd_ssl_certificate_key_file')
  $httpd_ssl_ca_certificate_file = hiera('httpd_ssl_ca_certificate_file', '')
  $httpd_log_dir = hiera('httpd_log_dir', '/var/log/httpd')
  $httpd_ssl_document_root = hiera('httpd_ssl_document_root', '/var/www/html')
  $httpd_ssl_cgibin_root = hiera('httpd_ssl_cgibin_root', '/var/www/cgi-bin')

  case $httpdVersion {
    /^2.4/:  { $mod_ssl_conf_template = 'mod_ssl.conf.2.4.erb' }
    /^2.2/:  { $mod_ssl_conf_template = 'mod_ssl.conf.2.2.erb' }
    default: { fail "unsupported HTTPd version: ${httpdVersion}" }
  }

  file { '/etc/httpd/conf.d/mod_ssl.conf' :
    ensure  => file,
    content => template("httpd/$mod_ssl_conf_template"),
    owner   => 'root',
    group   => 'root',
    mode    => 'u=rw,go=r',
    # TODO require => File['/etc/httpd/modules/mod_ssl.so'],
    require => Package['mod_ssl'],
  }
}
