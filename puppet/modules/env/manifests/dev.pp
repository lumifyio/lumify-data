class env::dev {
  include clavin

  file { '/etc/yum.repos.d/lumify.repo' :
    source => 'puppet:///modules/env/dev/lumify.repo',
    owner => 'vagrant',
    mode => 'u=rw,g=r,o=r',
  }

  package { [ 'lumify-ffmpeg', 'lumify-ccextractor', 'lumify-tesseract', 'lumify-tesseract-eng', 'lumify-opencv' ] :
    ensure => present,
    require => File['/etc/yum.repos.d/lumify.repo'],
  }

  file { "/etc/profile.d/opencv.sh":
    ensure   => file,
    source   => 'puppet:///modules/env/dev/opencv.sh',
    owner    => "root",
    group    => "root",
    force    => true,
    require  => Package['lumify-opencv'],
  }
}
