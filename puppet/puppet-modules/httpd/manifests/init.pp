class httpd (
  $ssl_only = 'false'
){
    package { 'httpd':
        ensure => present,
    }
    
    file { '/etc/httpd/conf/httpd.conf' :
      ensure  => file,
      content => template('httpd/httpd.conf.erb'),
      owner   => 'root',
      group   => 'root',
      mode    => 'u=rw,go=r',
      require => Package['httpd'],
    }
}
