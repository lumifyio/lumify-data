class ffmpeg::libvpx($prefix="/usr/local", $tmpdir="/usr/local/src") {
  require buildtools
  include macro
  include macro::git

  $srcdir = "${tmpdir}/libvpx"

  macro::git::clone { "libvpx-clone":
    url     => "http://git.chromium.org/webm/libvpx.git",
    path    => $srcdir,
    options => "--depth 1",
  }

  macro::git::checkout { 'libvpx-checkout':
    path    => $srcdir,
    branch  => "v1.2.0",
    require => Macro::Git::Clone["libvpx-clone"],
  }

  $configure  = "${srcdir}/configure --prefix=${prefix} --disable-examples"
  $make       = "/usr/bin/make -j${processorcount}"
  $install    = "/usr/bin/make install"
  $distclean  = "/usr/bin/make clean"
  $cmd        = "${configure} && ${make} && ${install} && ${distclean}"

  exec { 'libvpx-build' :
    cwd     => $srcdir,
    command => $cmd,
    creates => "${prefix}/lib/libvpx.a",
    require => Macro::Git::Checkout['libvpx-checkout'],
  }
}
