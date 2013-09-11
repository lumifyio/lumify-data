class ffmpeg::x264($prefix="/usr/local", $tmpdir="/usr/local/src") {
  require buildtools
  include macro

  $srcdir = "${tmpdir}/x264"

  macro::git-clone { "x264-clone":
    url     => "http://git.videolan.org/git/x264.git",
    path    => $srcdir,
  }

  macro::git-checkout { 'x264-checkout':
    path    => $srcdir,
    branch  => "stable",
    require => Macro::Git-clone["x264-clone"],
  }

  $configure = "${srcdir}/configure --prefix='${prefix}' --bindir='${prefix}/bin' --enable-static"
  $make      = "/usr/bin/make -j${processorcount}"
  $install   = "/usr/bin/make install"
  $distclean  = "/usr/bin/make distclean"
  $cmd       = "${configure} && ${make} && ${install} && ${distclean}"

  exec { 'x264-build' :
    cwd     => $srcdir,
    command => $cmd,
    creates => "${prefix}/bin/x264",
    require => Macro::Git-checkout['x264-checkout'],
  }
}
