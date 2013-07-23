class oozie($prefix = "/usr/lib/oozie") {
  require hadoop
  include macro

  $downloadpath = "/usr/local/src/ext-2.2.zip"

  package { "oozie":
    ensure  => installed,
    require => Package["hadoop-0.20"],
  }

  file { "oozie-extjs-dir":
    path    => "${prefix}/libext",
    ensure  => directory,
    require => Package["oozie"],
  }

  macro::download { "http://extjs.com/deploy/ext-2.2.zip":
    path    => $downloadpath,
  } -> macro::extract { $downloadpath:
    type    => "zip",
    path    => "${prefix}/libext",
    require => File["oozie-extjs-dir"],
  }
}
