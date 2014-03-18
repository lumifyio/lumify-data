class syslog::fw {
  firewall { '011 allow syslog' :
    proto  => udp,
    port   => 514,
    action => accept,
  }
}

class env::cluster::syslog::loggly {
  $loggly_token = hiera('loggly_token')
  $loggly_tag = hiera('loggly_tag')
  $facility = 'local3' # use '*' to send everything

  exec { 'configure format' :
    comand => "/bin/cat '$template LogglyFormat,\"<%pri%>%protocol-version% %timestamp:::date-rfc3339% %HOSTNAME% %app-name% %procid% %msgid% [${loggly_token}@41058 tag=\\"${loggly_tag}\\"] %msg%\n\"' >> /etc/rsyslog.conf",
    unless => "/bin/grep -q '$template LogglyFormat' /etc/rsyslog.conf",
    require => Package['rsyslog'],
  }

  exec { 'configure forward' :
    comand => "/bin/cat '${facility}.* @@logs-01.loggly.com:514;LogglyFormat' >> /etc/rsyslog.conf",
    unless => "/bin/grep -q '@@loggly.com:514' /etc/rsyslog.conf",
    require => Package['rsyslog'],
  }
}

class env::cluster::syslog inherits env::cluster::base {
  $facility = 'local3'

  package { 'rsyslog' :
    ensure => present,
  }

  include my_fw
  class { syslog::fw :
    stage => 'first',
  }

  exec { 'enable udp' :
    comand => "/bin/sed -i -e 's/#$ModLoad imudp/$ModLoad imudp/' -e 's/#$UDPServerRun 514/$UDPServerRun 514/' /etc/rsyslog.conf",
    unless => "/bin/grep -q '^$ModLoad imudp' /etc/rsyslog.conf",
    require => Package['rsyslog'],
  }

  exec { 'set max message size' :
    comand => "/bin/cat '$MaxMessageSize 64k' >> /etc/rsyslog.conf",
    unless => "/bin/grep -q '$MaxMessageSize 64k' /etc/rsyslog.conf",
    require => Package['rsyslog'],
  }

  exec { 'configure log file' :
    comand => "/bin/cat '${facility}.* /var/log/${facility}' >> /etc/rsyslog.conf",
    unless => "/bin/grep -q '${facility}.* /var/log/${facility}' /etc/rsyslog.conf",
    require => Package['rsyslog'],
  }
}