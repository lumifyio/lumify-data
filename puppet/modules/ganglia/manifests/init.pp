class ganglia {

  group { 'ganglia' :
    ensure => present,
  }

  user { 'ganglia' :
    ensure  => present,
    gid     => 'ganglia',
    require => Group['ganglia'],
  }
}
