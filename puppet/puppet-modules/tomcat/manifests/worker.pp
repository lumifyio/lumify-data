class tomcat::worker {
  include tomcat

  $catalina_home = $tomcat::home

  extract_tomcat { $catalina_home : }

  if $interfaces =~ /eth1/ {
    $worker_ip = $ipaddress_eth1
  } elsif $interfaces =~ /em2/ {
    $worker_ip = $ipaddress_em2
  } else {
    $worker_ip = $ipaddress_eth0
  }

  $mod_jk_workers = hiera_hash('mod_jk_workers')

  file { "${catalina_home}/conf/server.xml" :
    ensure  => file,
    content => template('tomcat/server.xml.worker.erb'),
    owner   => $user,
    group   => $group,
    mode    => "u=rw",
    require => Macro::Extract["extract-tomcat-${catalina_home}"],
  }

  upstart_tomcat { 'tomcat' : }
}
