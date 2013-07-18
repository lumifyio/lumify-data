class java($tmpdir = '/tmp', $version = '6u45', $arch = 'amd64') {
  include macro

  $pkg = "jdk-${version}-linux-${arch}"
  $dist = "${pkg}.rpm"
  $path = "${tmpdir}/${dist}"
  $javahome = "/usr/java/default"

  macro::download { "jdk-download":
    url  => "https://s3.amazonaws.com/RedDawn/${dist}",
    path => $path
  }

  package { "jdk":
    ensure   => installed,
    source   => $path,
    provider => "rpm",
    require  => Macro::Download["jdk-download"],
  }

  file { "/etc/profile.d/java_home.sh":
    ensure  => file,
    content => "export JAVA_HOME=${javahome}",
    require => Package['jdk'],
  }
}
require java