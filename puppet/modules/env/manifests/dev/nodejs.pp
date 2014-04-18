class env::dev::nodejs {
  package { [ 'nodejs', 'npm' ] :
    ensure => present,
  }

  define install_npm_module {
    exec { "npm-install-${name}" :
      command => "/usr/bin/npm install -g ${name}",
      unless  => "/usr/bin/npm list -g 2>/dev/null | /bin/grep ${name}@",
      require => Package['npm'],
    }
  }

  install_npm_module { [ 'bower', 'grunt', 'grunt-cli' ] : }
}
