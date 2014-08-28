class ganglia {
  require buildtools::epel

  group { 'ganglia' :
    ensure => present,
  }

  user { 'ganglia' :
    ensure  => present,
    gid     => 'ganglia',
    require => Group['ganglia'],
  }
}
