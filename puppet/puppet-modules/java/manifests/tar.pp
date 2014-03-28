class java::tar($dir, $creates, $tmpdir = '/tmp', $version = '7u51') {
  include macro

  case $architecture {
    'x86_64': { $tgz = "jdk-${version}-linux-x64.tar.gz" }
    default: { fail "unsupported architecture: ${architecture}" }
  }

  macro::download { "https://s3.amazonaws.com/RedDawn/puppet-repo/java/${tgz}" :
    path => "${tmpdir}/${tgz}",
  } -> macro::extract { "${tmpdir}/${tgz}" :
    path => $dir,
    creates => $creates,
  } -> file { $creates :
    ensure => directory,
    recurse => true,
    owner => 'root',
    group => 'root',
  }
}