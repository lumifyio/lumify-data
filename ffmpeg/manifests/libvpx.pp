class ffmpeg::libvpx($prefix="/usr/local/ffmpeg", $tmpdir="/usr/local/src") {
  require buildtools
  include macro

  $srcdir = "${tmpdir}/libvpx"
  
  macro::git-clone { "libvpx-clone":
    url     => "http://git.chromium.org/webm/libvpx.git",
    path    => $srcdir,
    options => "--depth 1",
  }
  
  macro::git-checkout { 'libvpx-checkout':
    path    => $srcdir,
    branch  => "v1.2.0",
    require => Macro::Git-clone["libvpx-clone"],
  }

  $configure  = "${srcdir}/configure --prefix=${prefix} --disable-examples"
  $make       = "/usr/bin/make"
  $install    = "/usr/bin/make install"
  $distclean  = "/usr/bin/make clean"
  $cmd        = "${configure} && ${make} && ${install} && ${distclean}"

  exec { 'libvpx-build' :
    cwd     => $srcdir,
    command => $cmd,
    creates => "${prefix}/lib/libvpx.a",
    require => Macro::Git-checkout['libvpx-checkout'],
  }
}
