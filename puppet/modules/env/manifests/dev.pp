class env::dev {
  file { '/etc/yum.repos.d/lumify.repo' :
    source => 'puppet:///modules/env/dev/lumify.repo',
    owner => 'vagrant',
    mode => 'u=rw,g=r,o=r',
  }

  package { [ 'lumify-ffmpeg',
              'lumify-ccextractor',
              'lumify-tesseract',
              'lumify-tesseract-eng',
              'lumify-opencv',
              'lumify-pocketsphinx',
            ] :
    ensure => present,
    require => File['/etc/yum.repos.d/lumify.repo'],
  }

  package { 'sox' :
    ensure => present,
    require => Package['lumify-ffmpeg'],
  }

  file { '/etc/profile.d/opencv.sh' :
    ensure   => file,
    source   => 'puppet:///modules/env/dev/opencv.sh',
    owner    => "root",
    group    => "root",
    force    => true,
    require  => Package['lumify-opencv'],
  }
}
