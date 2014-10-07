class httpd (
  $httpdVersion = '2.2.15',
  $ssl_only = 'false'
){
  package { 'httpd' :
    ensure => $httpdVersion,
  }

  case $httpdVersion {
    /^2.4/:  { $http_conf_template = 'httpd.conf.2.4.erb' }
    /^2.2/:  { $http_conf_template = 'httpd.conf.2.2.erb' }
    default: { fail "unsupported HTTPd version: ${httpdVersion}" }
  }

  file { '/etc/httpd/conf/httpd.conf' :
    ensure  => file,
    content => template("httpd/$http_conf_template"),
    owner   => 'root',
    group   => 'root',
    mode    => 'u=rw,go=r',
    require => Package['httpd'],
  }
}
