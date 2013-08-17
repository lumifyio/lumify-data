class tesseract($prefix="/usr/local", $tmpdir="/usr/local/src") {
  require tesseract::leptonica
  require buildtools
  include macro

  $srcdir = "${tmpdir}/tesseract-ocr"

  macro::download { "tesseract-download":
    url  => "https://tesseract-ocr.googlecode.com/files/tesseract-ocr-3.02.02.tar.gz",
    path => "${tmpdir}/tesseract-ocr-3.02.02.tar.gz",
  } -> macro::extract { 'extract-tesseract':
    file => "${tmpdir}/tesseract-ocr-3.02.02.tar.gz",
    path => $tmpdir,
  }

  file { "${prefix}/share/tessdata/":
    ensure => directory,
  }

  macro::download { "tesseract-eng-download":
    url  => "https://tesseract-ocr.googlecode.com/files/tesseract-ocr-3.02.eng.tar.gz",
    path => "${tmpdir}/tesseract-ocr-3.02.eng.tar.gz",
  } -> macro::extract { 'extract-tesseract-eng':
    file => "${tmpdir}/tesseract-ocr-3.02.eng.tar.gz",
    path => "${prefix}/share/tessdata/",
    options => "--strip-components=2",
    require => File["${prefix}/share/tessdata/"],
  }

  $autogen    = "${srcdir}/autogen.sh"
  $configure  = "${srcdir}/configure --prefix=${prefix}"
  $make       = "/usr/bin/make"
  $install    = "/usr/bin/make install"
  $distclean  = "/usr/bin/make distclean"
  $cmd        = "${autogen} && ${configure} && ${make} && ${install} && ${distclean}"

  exec { 'tesseract-build' :
    cwd     => $srcdir,
    command => $cmd,
    environment => "PKG_CONFIG_PATH=${prefix}/lib/pkgconfig",
    timeout => 0,
    creates => "${prefix}/lib/libtesseract.a",
    require => Macro::Extract['extract-tesseract'],
  }
}
