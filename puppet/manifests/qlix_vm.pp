include env::demo

include httpd
include httpd::mod_jk
include httpd::mod_ssl

class { 'java::tar' :
  version => '7u51',
  dir     => '/opt',
  creates => '/opt/jdk1.7.0_51'
}

# http://docs.puppetlabs.com/puppet/latest/reference/lang_classes.html#inheritance
$tomcat_java_home = '/opt/jdk1.7.0_51'
include tomcat::worker

class { 'mysql::server' :
  remove_default_accounts => true,
  restart => true,
  override_options => { 'mysqld' => { 'bind-address' => '0.0.0.0' } }
}

mysql::db { 'lumify' :
  user     => 'lumify',
  password => 'lumify',
  host     => '%',
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
