class tomcat::worker inherits tomcat {

    if $interfaces =~ /eth1/ {
      $worker_ip = $ipaddress_eth1
    } elsif $interfaces =~ /em2/ {
      $worker_ip = $ipaddress_em2
    } else {
      $worker_ip = $ipaddress_eth0
    }
    
    $mod_jk_workers = hiera_hash("mod_jk_workers")
    
    file { 'server.xml':
      ensure  => file,
      path    => "${home}/conf/server.xml",
      content => template('tomcat/server.xml.worker.erb'),
      owner   => $user,
      group   => $group,
      mode    => "u=rw",
      require => Macro::Extract['extract-tomcat'],
    } 
}
