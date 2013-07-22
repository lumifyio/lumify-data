class ffmpeg::libmp3lame($prefix="/usr/local", $tmpdir="/usr/local/src") {
  require buildtools
  include macro

  $srcdir = "${tmpdir}/lame-3.99.5"

  macro::download { "libmp3lame-download":
    url     => "http://downloads.sourceforge.net/project/lame/lame/3.99/lame-3.99.5.tar.gz",
    path    => "${tmpdir}/lame-3.99.5.tar.gz",
  } -> macro::extract { 'extract-libmp3lame':
    file => "${tmpdir}/lame-3.99.5.tar.gz",
    path => $tmpdir,
  }

  $configure  = "${srcdir}/configure --prefix=${prefix} --bindir=${prefix}/bin --enable-nasm"
  $make       = "/usr/bin/make"
  $install    = "/usr/bin/make install"
  $distclean  = "/usr/bin/make distclean"
  $cmd        = "${configure} && ${make} && ${install} && ${distclean}"

  exec { 'libmp3lame-build' :
    cwd     => $srcdir,
    command => $cmd,
    creates => "${prefix}/bin/lame",
    require => Macro::Extract['extract-libmp3lame'],
  }
}
