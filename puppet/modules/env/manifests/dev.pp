class env::dev {
  file { '/etc/yum.repos.d/lumify.repo' :
    source => 'puppet:///modules/env/dev/lumify.repo',
    owner => 'vagrant',
    mode => 'u=rw,g=r,o=r',
  }

  package { [ 'lumify-ffmpeg', 'lumify-ccextractor', 'lumify-tesseract', 'lumify-tesseract-eng', 'lumify-opencv' ] :
    ensure => present,
    require => File['/etc/yum.repos.d/lumify.repo'],
  }
}
