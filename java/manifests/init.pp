class java($tmpdir = '/tmp', $version = '6u45') {
  include macro

  case $architecture {
    'x86_64': { $pkg = "jdk-${version}-linux-x64" }
    'i386':   { $pkg = "jdk-${version}-linux-i586" }
    default:  { fail "unsupported architecture: ${architecture}" }
  }
  $dist = "${pkg}.rpm"
  $path = "${tmpdir}/${dist}"
  $java_home = hiera("java_home")

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
    content => "export JAVA_HOME=${java_home}
                export PATH=\$PATH:\$JAVA_HOME/bin",
    require => Package['jdk'],
  }
}
