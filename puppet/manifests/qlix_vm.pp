include env::demo

include httpd
include httpd::mod_jk
include httpd::mod_ssl
include tomcat::worker

class { 'mysql::server' :
  remove_default_accounts => true,
  restart => true,
}

mysql::db { 'lumify' :
  user     => 'lumify',
  password => 'lumify',
  host     => 'localhost',
  grant    => ['ALL'],
  require  => Class[mysql::server],
}

file { '/opt/lumify/config/lumify.cert.pem' :
  ensure => file,
  source => 'file:///vagrant/config/ssl/lumify.cert.pem',
  owner   => 'root',
  group   => 'root',
  mode    => 'u=rw,go=r',
}

file { '/opt/lumify/config/lumify.key.pem' :
  ensure => file,
  source => 'file:///vagrant/config/ssl/lumify.key.pem',
  owner   => 'root',
  group   => 'root',
  mode    => 'u=rw,go=r',
}
