class ganglia {
  group { 'ganglia' :
    ensure => present,
  }

  user { 'ganglia' :
    ensure  => present,
    gid     => 'ganglia',
    home    => '/opt/ganglia',
    require => Group['ganglia'],
  }
}
