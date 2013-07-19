class ffmpeg (
  $tmpdir="/tmp"
) {
  $installdir="/opt"
  $homedir = "${installdir}/ffmpeg"
  $srcdir = "${tmpdir}/ffmpeg-source"
  $bindir = "${installdir}/bin"

  exec { 'epel':
    command => '/bin/rpm -ivH http://dl.fedoraproject.org/pub/epel/6/x86_64/epel-release-6-8.noarch.rpm',
    unless => '/bin/rpm -q epel-release-6-8',
  }

  package { 'yasm' :
    ensure => present,
    require => Exec['epel'],
  }

  package { 'git' :
    ensure => present,
  }

  file { "ffmpeg-srcdir":
    path   => $srcdir,
    ensure => directory,
  }

#  exec { 'ffmpeg-clone' :
#    command => "/usr/bin/git clone git://source.ffmpeg.org/ffmpeg.git ${srcdir} && cd ${srcdir} && /usr/bin/git checkout release/0.11",
#    unless => "/usr/bin/test -d ${srcdir}",
#    require => [Package['git'], File["ffmpeg-srcdir"]],
#  }

  exec { 'x264-clone' :
    command => "/usr/bin/git clone --depth 1 git://git.videolan.org/x264 && cd x264 && /usr/bin/git checkout stable",
    cwd     => $srcdir,
    unless  => "/usr/bin/test -d ${srcdir}/x264",
    require => [Package['git'], File["ffmpeg-srcdir"]],
  }

  exec { 'x264-build' :
    cwd => "${srcdir}/x264",
    command => "${srcdir}/x264/configure --prefix=${homedir} --bindir=${bindir} --enable-static && /usr/bin/make && /usr/bin/make install && /usr/bin/make distclean",
#    unless => "/usr/bin/test -x ${homedir}/bin/ffmpeg",
    require => Exec['x264-clone'],
  }

#  exec { 'ffmpeg-compile' :
#    cwd => $srcdir,
#    command => "${srcdir}/configure --disable-static --disable-debug --enable-avfilter --enable-pthreads --enable-postproc --enable-shared --enable-pic --disable-gpl --prefix=${homedir} && /usr/bin/make && /usr/bin/make install",
#    unless => "/usr/bin/test -x ${homedir}/bin/ffmpeg",
#    require => [Exec['ffmpeg-clone'], Exec['x264-build']],
#  }
#
#  exec { 'ffmpeg-qt-fast-start' :
#    cwd => "${srcdir}/tools",
#    command => "/usr/bin/make qt-faststart && cp ${srcdir}/tools/qt-faststart ${homedir}/bin/qt-faststart",
#    unless => "/usr/bin/test -x ${homedir}/bin/qt-faststart",
#    require => Exec['ffmpeg-compile'],
#  }

#  file { "${bindir}/ffmpeg":
#    ensure => link,
#    target => "${homedir}/bin/ffmpeg",
#    require => [Exec['ffmpeg-compile'], Exec['ffmpeg-qt-fast-start']]
#  }
#
#  file { "${bindir}/ffprobe":
#    ensure => link,
#    target => "${homedir}/bin/ffprobe",
#    require => [Exec['ffmpeg-compile'], Exec['ffmpeg-qt-fast-start']]
#  }
#
#  file { "${bindir}/ffserver":
#    ensure => link,
#    target => "${homedir}/bin/ffserver",
#    require => [Exec['ffmpeg-compile'], Exec['ffmpeg-qt-fast-start']]
#  }
#
#  file { "${bindir}/qt-faststart":
#    ensure => link,
#    target => "${homedir}/bin/qt-faststart",
#    require => [Exec['ffmpeg-compile'], Exec['ffmpeg-qt-fast-start']]
#  }
}
require ffmpeg
