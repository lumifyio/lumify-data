class jetty(
  $version='9.2.5.v20141112'
){
  require java

  $jetty_insecure_listen_port = hiera('jetty_insecure_listen_port', 8080)
  $jetty_confidential_listen_port = hiera('jetty_confidential_listen_port', 8443)
  $jetty_confidential_redirect_port = hiera('jetty_confidential_redirect_port', 443)
  $jetty_key_store_path = hiera('jetty_key_store_path')
  $jetty_key_store_password = hiera('jetty_key_store_password')
  $jetty_trust_store_path = hiera('jetty_trust_store_path')
  $jetty_trust_store_password = hiera('jetty_trust_store_password')
  $jetty_client_auth = hiera('jetty_client_auth')

  group { 'jetty' :
    ensure => present,
  }

  user { 'jetty' :
   ensure  => present,
   gid     => 'jetty',
   home    => '/opt/jetty',
   require => Group['jetty'],
  }

  macro::download { 'jetty-download':
    url  => "http://eclipse.org/downloads/download.php?file=/jetty/${version}/dist/jetty-distribution-${version}.tar.gz&r=1",
    path => "/tmp/jetty-distribution-${version}.tar.gz",
  } -> macro::extract { 'jetty-extract':
    file    => "/tmp/jetty-distribution-${version}.tar.gz",
    path    => "/opt",
    creates => "/opt/jetty-distribution-${version}",
  }

  file { "/opt/jetty-distribution-${version}" :
    ensure  => directory,
    owner   => 'jetty',
    group   => 'jetty',
    recurse => true,
    require => [ User['jetty'], Macro::Extract['jetty-extract'] ],
  }

  file { '/opt/jetty' :
    ensure  => link,
    target  => "/opt/jetty-distribution-${version}",
    require => File["/opt/jetty-distribution-${version}"],
  }

  file { '/etc/init.d/jetty' :
    ensure  => link,
    target  => '/opt/jetty/bin/jetty.sh',
    require => File['/opt/jetty'],
  }

  file { '/opt/jetty/contexts' :
    ensure  => directory,
    require => File['/opt/jetty'],
  }

  file { '/opt/jetty/etc/jetty.xml' :
    ensure  => file,
    content => template('jetty/jetty.xml.erb'),
    require => File['/opt/jetty'],
  }

  file { '/opt/jetty/etc/jetty-http.xml' :
    ensure  => file,
    content => template('jetty/jetty-http.xml.erb'),
    require => File['/opt/jetty'],
  }

  file { '/opt/jetty/etc/jetty-https.xml' :
    ensure  => file,
    content => template('jetty/jetty-https.xml.erb'),
    require => File['/opt/jetty'],
  }

  file { '/opt/jetty/etc/jetty-ssl.xml' :
    ensure  => file,
    content => template('jetty/jetty-ssl.xml.erb'),
    require => File['/opt/jetty'],
  }

  file { '/etc/default/jetty' :
    ensure  => file,
    content => 'JETTY_HOME=/opt/jetty; JETTY_USER=jetty',
  }

  service { 'jetty' :
    enable  => true,
    ensure  => running,
    require => [ File['/etc/init.d/jetty'],
                 File['/etc/default/jetty'],
                 File['/opt/jetty/etc/jetty.xml'],
                 File['/opt/jetty/etc/jetty-http.xml'],
                 File['/opt/jetty/etc/jetty-https.xml'],
                 File['/opt/jetty/etc/jetty-ssl.xml'],
                 Exec['jetty-disable-contexts'],
                 Exec['jetty-disable-webapps']
               ],
  }

  file { '/root/deploy-webapps.sh' :
   source => 'puppet:///modules/jetty/deploy-webapps.sh',
   mode => 'u=rwx,go=',
  }

  exec { 'jetty-deploy-webapps' :
    cwd     => '/root',
    command => '/root/deploy-webapps.sh',
    require => [ Service['jetty'], File['/root/deploy-webapps.sh'] ],
  }
}
