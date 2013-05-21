class zookeeper::config ($user = 'zk', $group = 'hadoop') {

  file { '/var/zookeeper' :
    ensure => 'directory',
    owner => "${user}",
    group => "${group}",
    mode => 'u=rwx,g=rwxs,o=',
    require => [ User["${user}"], Group["${group}"] ],
  }

}
