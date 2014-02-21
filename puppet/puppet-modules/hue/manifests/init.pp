class hue(
  $user = "hue",
  $group = "hue",
  $home = "/usr/share/hue",
) {
  
  $namenode_hostname = hiera("namenode_hostname")
  $http_host  = hiera("hue_http_host")
  $http_port  = hiera("hue_http_port")
  $timezone   = hiera("hue_timezone")
  $secret_key = hiera("hue_secret_key")
  
  package { ['hue-common', 'hue-about', 'hue-help', 'hue-filebrowser', 'hue-jobbrowser', 'hue-useradmin']:
    ensure  => installed,
    require => Class['repo::cloudera::cdh4'],    
  }

  package { 'hue-server':
    ensure => installed,
    require => Package['hue-common'],
  }  
  
  package { 'hue-plugins':
    ensure => installed,
    require => Package['hue-common'],
  }
  
  file { $home:
    ensure => "directory",
    owner  => $user,
    require => Package['hue-common'],
  }
    
  exec { 'copy-jobtracker-hue-plugin' :
    cwd => $home,
    path => "/bin",
    command => 'cp desktop/libs/hadoop/java-lib/hue-plugins-*.jar /usr/lib/hadoop-0.20-mapreduce/lib',
    creates => '/usr/lib/hadoop-0.20-mapreduce/lib/hue-plugins-*.jar',
    require => Package['hue-common'],
  }
  
  group { $group :
    ensure => present,
    require => Package[$hadoop::pkg],
  }

  user { $user :
    ensure  => present,
    gid     => $group,
    home    => $home,
    groups  => ["hadoop"],
    require => Group[$group],
  }
  
  file { '/etc/hue/hue.ini':
    content => template("hue/hue.ini.erb"),
    require => Package['hue-server'],
  }  
}
