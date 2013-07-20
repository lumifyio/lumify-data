class ffmpeg::libopus($prefix="/usr/local/ffmpeg", $tmpdir="/usr/local/src") {
  require buildtools
  include macro

  $srcdir = "${tmpdir}/opus-1.0.3"

  macro::download { "libopus-download":
    url     => "http://downloads.xiph.org/releases/opus/opus-1.0.3.tar.gz",
    path    => "${tmpdir}/opus-1.0.3.tar.gz",
  } -> macro::extract { 'extract-libopus':
    file => "${tmpdir}/opus-1.0.3.tar.gz",
    path => $tmpdir,
  }

  $configure  = "${srcdir}/configure --prefix=${prefix} --disable-shared"
  $make       = "/usr/bin/make"
  $install    = "/usr/bin/make install"
  $distclean  = "/usr/bin/make distclean"
  $cmd        = "${configure} && ${make} && ${install} && ${distclean}"

  exec { 'libopus-build' :
    cwd => $srcdir,
    command => $cmd,
    creates => "${prefix}/lib/libopus.a",
    require => Macro::Extract['extract-libopus'],
  }
}
