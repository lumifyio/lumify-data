class ffmpeg::libfdkaac($prefix="/usr/local", $tmpdir="/usr/local/src") {
  require buildtools
  include macro
  include macro::git

  $srcdir = "${tmpdir}/fdk-aac"

  macro::git::clone { "libfdkaac-clone":
    url     => "https://github.com/mstorsjo/fdk-aac.git",
    path    => $srcdir,
    options => "--depth 1",
  }

  macro::git::checkout { 'libfdkaac-checkout':
    path   => $srcdir,
    branch => "v0.1.1",
    require => Macro::Git::Clone["libfdkaac-clone"],
  }

  $autoreconf = "/usr/bin/autoreconf -fiv"
  $configure  = "${srcdir}/configure --prefix=${prefix}"
  $make       = "/usr/bin/make -j${processorcount}"
  $install    = "/usr/bin/make install"
  $distclean  = "/usr/bin/make distclean"
  $cmd        = "${autoreconf} && ${configure} && ${make} && ${install} && ${distclean}"

  exec { 'libfdkaac-build' :
    cwd => $srcdir,
    command => $cmd,
    creates => "${prefix}/lib/libfdk-aac.a",
    require => Macro::Git::Checkout['libfdkaac-checkout'],
  }
}
