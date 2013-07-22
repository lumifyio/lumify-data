class opencv {
  require ffmpeg

  package { "opencv":
    ensure => installed,
  }
}