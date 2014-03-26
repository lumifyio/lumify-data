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