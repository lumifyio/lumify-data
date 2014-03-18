class syslog::fw {
  firewall { '011 allow syslog' :
    proto  => udp,
    port   => 514,
    action => accept,
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
    notify  => Exec['restart_rsyslogd'],
  }

  exec { 'set max message size' :
    comand => "/bin/echo '$MaxMessageSize 64k' >> /etc/rsyslog.conf",
    unless => "/bin/grep -q '$MaxMessageSize 64k' /etc/rsyslog.conf",
    require => Package['rsyslog'],
    notify  => Exec['restart_rsyslogd'],
  }

  exec { 'configure log file' :
    comand => "/bin/echo '${facility}.* /var/log/${facility}' >> /etc/rsyslog.conf",
    unless => "/bin/grep -q '${facility}.* /var/log/${facility}' /etc/rsyslog.conf",
    require => Package['rsyslog'],
    notify  => Exec['restart_rsyslogd'],
  }

  exec { 'restart_rsyslogd' :
    command     => '/sbin/service rsyslog restart',
    refreshonly => true,
  }
}