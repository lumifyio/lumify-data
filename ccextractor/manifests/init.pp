class ccextractor (
  $version="0.66",
  $tmpdir="/tmp",
  $installdir="/opt/bin"
) {
  include macro

  $srcdir = "${tmpdir}/ccextractor.${version}/linux"

  macro::download { "ccextractor-download":
    url  => "http://downloads.sourceforge.net/project/ccextractor/ccextractor/${version}/ccextractor.src.${version}.zip",
    path => "${tmpdir}/ccextractor.src.${version}.zip",
  } -> macro::extract { 'extract-ccextractor':
    file => "${tmpdir}/ccextractor.src.${version}.zip",
    type => "zip",
    path => $tmpdir,
  }

  exec { "build-ccextractor":
    command => "${srcdir}/build",
    cwd     => "${srcdir}",
    creates => "${srcdir}/ccextractor",
    unless  => "/usr/bin/test -f ${srcdir}/ccextractor",
    require => Macro::Extract['extract-ccextractor'],
  }

  file { 'ccextractor-bin-dir':
    path   => "${installdir}",
    ensure => directory,
  }

  exec { "copy-ccextractor-to-bin":
    command => "/bin/cp ccextractor ${installdir}",
    cwd     => $srcdir,
    unless  => "/usr/bin/test -f ${installdir}/ccextractor",
    require => File['ccextractor-bin-dir'],
  }
}
