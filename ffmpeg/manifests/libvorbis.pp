class ffmpeg::libvorbis($prefix="/usr/local", $tmpdir="/usr/local/src") {
  require buildtools
  require libogg
  include macro

  $srcdir = "${tmpdir}/libvorbis-1.3.3"

  macro::download { "libvorbis-download":
    url  => "http://downloads.xiph.org/releases/vorbis/libvorbis-1.3.3.tar.gz",
    path => "${tmpdir}/libvorbis-1.3.3.tar.gz",
  } -> macro::extract { 'extract-libvorbis':
    file => "${tmpdir}/libvorbis-1.3.3.tar.gz",
    path => $tmpdir,
  }
  
  $configure  = "${srcdir}/configure --prefix=${prefix} --with-ogg=${prefix}"
  $make       = "/usr/bin/make"
  $install    = "/usr/bin/make install"
  $distclean  = "/usr/bin/make distclean"
  $cmd        = "${configure} && ${make} && ${install} && ${distclean}"
  
  exec { 'libvorbis-build' :
    cwd     => $srcdir,
    command => $cmd,
    environment => "LD_LIBRARY_PATH=\$LD_LIBRARY_PATH:/${prefix}/lib",
    creates => "${prefix}/lib/libvorbis.a",
    require => Macro::Extract['extract-libvorbis'],
  }
}
