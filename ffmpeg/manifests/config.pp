class ffmpeg::config () {

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

  exec { 'ffmpeg-clone' :
    command => '/usr/bin/git clone git://source.ffmpeg.org/ffmpeg.git /opt/ffmpeg-source && cd /opt/ffmpeg-source && /usr/bin/git checkout release/0.11',
    unless => '/usr/bin/test -d /opt/ffmpeg-source',
    require => Package['git'],
  }

  exec { 'ffmpeg-compile' :
    cwd => '/opt/ffmpeg-source',
    command => '/opt/ffmpeg-source/configure --disable-static --disable-debug --enable-avfilter --enable-pthreads --enable-postproc --enable-shared --enable-pic --disable-gpl --prefix=/opt/ffmpeg && /usr/bin/make && /usr/bin/make install',
    unless => '/usr/bin/test -x /opt/ffmpeg/bin/ffmpeg',
    require => Exec['ffmpeg-clone'],
  }
}
