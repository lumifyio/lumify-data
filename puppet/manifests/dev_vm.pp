include env::demo
include env::dev

package { [ 'nodejs', 'npm', 'zip' ] :
  ensure => present,
}

exec { 'npm-install-bower' :
  command => '/usr/bin/npm install -g bower',
  unless => '/usr/bin/npm list -g 2>/dev/null | /bin/grep bower@',
  require => Package['npm'],
}

exec { 'npm-install-grunt' :
  command => '/usr/bin/npm install -g grunt',
  unless => '/usr/bin/npm list -g 2>/dev/null | /bin/grep grunt@',
  require => Package['npm'],
}

exec { 'npm-install-grunt-cli' :
  command => '/usr/bin/npm install -g grunt-cli',
  unless => '/usr/bin/npm list -g 2>/dev/null | /bin/grep grunt-cli@',
  require => Package['npm'],
}
