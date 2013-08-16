node "ip-10-0-3-10" {
  class { buildtools::epel :
    proxy_url => 'disabled',
  }

  package { "tinyproxy" :
    ensure => present,
    require => Exec["epel"],
  }

  exec { "tinyproxy-configure-port" :
    command => "/bin/sed -i 's/Port 8888/Port 8080/' /etc/tinyproxy/tinyproxy.conf",
    unless => "/bin/grep -q 'Port 8080' /etc/tinyproxy/tinyproxy.conf",
    require => Package["tinyproxy"],
  }

  exec { "tinyproxy-configure-allow" :
    command => "/bin/sed -i 's|Allow 127.0.0.1|Allow 10.0.3.0/24|' /etc/tinyproxy/tinyproxy.conf",
    unless => "/bin/grep -q 'Allow 10.0.3.0/24' /etc/tinyproxy/tinyproxy.conf",
    require => Package["tinyproxy"],
  }

  service { "tinyproxy" :
    enable => true,
    ensure => running,
    require => [ Exec["tinyproxy-configure-port"], Exec["tinyproxy-configure-allow"] ],
  }
}

node "ip-10-0-3-50" {
  include role::hadoop::namenode # includes jobtracker
  include role::blur::controller
}

node "ip-10-0-3-51" {
  include role::hadoop::secondarynamenode
  include role::accumulo::head
}

node "ip-10-0-3-101" {
  include role::hadoop::datanode # includes zookeeper, tasktracker, and native tools
  include role::accumulo::node
  include role::blur::shard
}

node "ip-10-0-3-102" {
  include role::hadoop::datanode
  include role::accumulo::node
  include role::blur::shard
}

node "ip-10-0-3-103" {
  include role::hadoop::datanode
  include role::accumulo::node
  include role::blur::shard
}

node "ip-10-0-3-200" {
  include role::webapp
}
