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
    url  => "http://s3.amazonaws.com/red-dawn/puppet-repo/java/${dist}",
    path => $path
  }

  package { "jdk":
    ensure   => installed,
    source   => $path,
    provider => "rpm",
    require  => Macro::Download["jdk-download"],
  }

  file { "/etc/profile.d/java.sh":
    ensure  => file,
    content => template('java/java.sh.erb'),
    require => Package['jdk'],
  }
}
