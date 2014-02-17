class redis(
  $tmpdir = "/usr/local/src",
  $user = "redis",
  $group = "redis",
  $home_dir = "/opt/redis") {
  include macro
  require buildtools
	
  $redisVersion = "2.8.5"
	
  $srcdir = "${tmpdir}/redis-${redisVersion}"
  $persist_dir = "${home_dir}/data"
	
	macro::download { "redis-download":
  	url  => "http://download.redis.io/releases/redis-${redisVersion}.tar.gz",
    path => "${tmpdir}/redis-${redisVersion}.tar.gz",
  } -> macro::extract { 'extract-redis':
    file    => "${tmpdir}/redis-${redisVersion}.tar.gz",
    path    => $tmpdir,
    creates => "${tmpdir}/redis-${redisVersion}",
  }
  
  $redis_bind_addresses = hiera('redis_bind_addresses') 
  $masterhost = hiera('redis_master_host','localhost')
  $masterport = hiera('redis_master_port','6379')

  $make    = "/usr/bin/make"
  $install = "/usr/bin/make install"
  $bin = "/usr/local/bin"
  $conf = "/etc/redis.conf"
  $slave = false
  
  group { $group :
    ensure => present,
  }

  user { $user :
    ensure  => present,
    gid     => $group,
    home    => $home,
    require => Group[$group],
  }

  exec { 'redis-build' :
		cwd     => $srcdir,
		command => "${make} && ${install}",
		creates => "${bin}/redis-server",
		require => Macro::Extract['extract-redis'],
  }
  
  file { $conf:
    ensure  => file,
    content => template("redis/redis.conf.erb"),
    owner   => $user,
    group   => $group,
    require => User[$user],    
  }
  
  file { "/etc/init/redis-server.conf":
    ensure  => file,
    content => template("redis/upstart.conf.erb"),
    owner   => $user,
    group   => $group,
    require => Exec['redis-build'],
  }
  
  file { [$home_dir, $persist_dir]:
    ensure  => directory,
    owner   => $user,
    group   => $group,
    recurse => true,
    require => User[$user],
  }
}