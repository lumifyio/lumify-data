class ffmpeg::libtheora($prefix="/opt/ffmpeg", $tmpdir="/tmp") {
  require buildtools
  require libogg
  include macro

  $srcdir = "${tmpdir}/libtheora-1.1.1"

  macro::download { "libtheora-download":
    url     => "http://downloads.xiph.org/releases/theora/libtheora-1.1.1.tar.gz",
    path    => "${tmpdir}/libtheora-1.1.1.tar.gz",
  } -> macro::extract { 'extract-libtheora':
    file => "${tmpdir}/libtheora-1.1.1.tar.gz",
    path => $tmpdir,
  }
  
  $configure  = "${srcdir}/configure --prefix=${prefix} --with-ogg=${prefix} --disable-examples --disable-shared --disable-sdltest --disable-vorbistest"
  $make       = "/usr/bin/make"
  $install    = "/usr/bin/make install"
  $distclean  = "/usr/bin/make distclean"
  $cmd        = "${configure} && ${make} && ${install} && ${distclean}"
  
  exec { 'libtheora-build' :
    cwd => $srcdir,
    command => $cmd,
    creates => "${prefix}/lib/libtheora.a",
    require => Macro::Extract['extract-libtheora'],
  }
}
