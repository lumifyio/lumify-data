class tomcat (
  $version = '7.0.52',
  $user = 'tomcat',
  $group = 'tomcat',
  $home = '/opt/tomcat',
  $tmpdir = '/tmp',
  $tgz = "${tmpdir}/apache-tomcat-${version}.tar.gz"
) {
  include macro
  require java

  group { $group :
    ensure => present,
  }

  user { $user :
    ensure  => present,
    gid     => $group,
    home    => $home,
    require => Group[$group],
  }

  macro::download { 'tomcat-download' :
    url  => "http://archive.apache.org/dist/tomcat/tomcat-7/v${version}/bin/apache-tomcat-${version}.tar.gz",
    path => $tomcat::tgz,
  }

  define extract_tomcat (
    $catalina_home = $title
  ) {
    file { $catalina_home :
      ensure => directory,
      owner   => $tomcat::user,
      group   => $tomcat::group,
      recurse => true,
    }
    macro::extract { "extract-tomcat-${catalina_home}" :
      file    => $tomcat::tgz,
      path    => $catalina_home,
      options => '--strip-components=1',
      creates => "${catalina_home}/LICENSE",
      require => [ Macro::Download['tomcat-download'], File[$catalina_home] ],
    }
  }

  define upstart_tomcat (
    $service_name = $title,
    $user = $tomcat::user,
    $group = $tomcat::group
  ) {
    if $tomcat_java_home == undef {
      $tomcat_java_home = hiera('java_home', '')
    }
    $tomcat_java_opts = hiera('tomcat_java_opts', '')

    file { "/etc/init/${service_name}.conf" :
      ensure  => file,
      content => template('tomcat/upstart.conf.erb'),
    }
  }
}
