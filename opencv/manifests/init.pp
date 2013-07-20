class opencv {
  package { "opencv":
    ensure => installed,
  }

  package { "opencv-devel":
    ensure  => installed,
    require => Package['opencv'],
  }
}