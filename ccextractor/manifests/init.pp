class ccextractor (
  $version="0.66",
  $tmpdir="/usr/local/src",
  $installdir="/usr/local/ccextractor"
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

  file { 'ccextractor-dir':
    path   => "${installdir}",
    ensure => directory,
  }

  file { 'ccextractor-bin-dir':
    path    => "${installdir}/bin",
    ensure  => directory,
    require => File['ccextractor-dir'],
  }

  exec { "copy-ccextractor-to-bin":
    command => "/bin/cp ccextractor ${installdir}/bin",
    cwd     => $srcdir,
    unless  => "/usr/bin/test -f ${installdir}/bin/ccextractor",
    require => [Exec['build-ccextractor'], File['ccextractor-bin-dir']],
  }
}
