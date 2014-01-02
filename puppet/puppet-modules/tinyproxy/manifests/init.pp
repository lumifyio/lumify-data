class tinyproxy {
  package { "tinyproxy" :
    ensure => present,
    require => Exec["epel"],
  }

  exec { "tinyproxy-configure-port" :
    command => "/bin/sed -i 's/Port 8888/Port 8080/' /etc/tinyproxy/tinyproxy.conf",
    unless => "/bin/grep -q 'Port 8080' /etc/tinyproxy/tinyproxy.conf",
    require => Package["tinyproxy"],
  }

  exec { "tinyproxy-configure-loglevel" :
    command => "/bin/sed -i 's/LogLevel Info/LogLevel Connect/' /etc/tinyproxy/tinyproxy.conf",
    unless => "/bin/grep -q 'LogLevel Connect' /etc/tinyproxy/tinyproxy.conf",
    require => Package["tinyproxy"],
  }

  exec { "tinyproxy-configure-allow" :
    command => "/bin/sed -i 's|Allow 127.0.0.1|Allow 10.0.3.0/24|' /etc/tinyproxy/tinyproxy.conf",
    unless => "/bin/grep -q 'Allow 10.0.3.0/24' /etc/tinyproxy/tinyproxy.conf",
    require => Package["tinyproxy"],
  }

  service { "tinyproxy" :
    enable => true,
    ensure => running,
    require => [ Exec["tinyproxy-configure-port"], Exec["tinyproxy-configure-allow"] ],
  }
}
