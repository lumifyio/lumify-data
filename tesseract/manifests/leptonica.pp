class tesseract::leptonica($prefix="/usr/local", $tmpdir="/usr/local/src") {
  require buildtools
  include macro

  $srcdir = "${tmpdir}/leptonica-1.69"

  macro::download { "leptonica-download":
    url  => "http://www.leptonica.org/source/leptonica-1.69.tar.gz",
    path => "${tmpdir}/leptonica-1.69.tar.gz",
  } -> macro::extract { 'extract-leptonica':
    file => "${tmpdir}/leptonica-1.69.tar.gz",
    path => $tmpdir,
  }

  $configure  = "${srcdir}/configure --prefix=${prefix}"
  $make       = "/usr/bin/make"
  $install    = "/usr/bin/make install"
  $distclean  = "/usr/bin/make distclean"
  $cmd        = "${configure} && ${make} && ${install} && ${distclean}"

  exec { 'leptonica-build' :
    cwd     => $srcdir,
    command => $cmd,
    creates => "${prefix}/lib/liblept.a",
    require => Macro::Extract['extract-leptonica'],
  }
}
