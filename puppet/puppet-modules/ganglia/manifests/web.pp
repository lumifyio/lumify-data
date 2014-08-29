class ganglia::web(
  $ganglia_web_version = '3.6.2',
) inherits ganglia {
  include macro

  ensure_resource('package', 'httpd', {'ensure' => 'present' })

  $ganglia_web_tgz_file = "/tmp/ganglia-web-${ganglia_web_version}.tar.gz"
  $ganglia_web_dir = "/usr/share/ganglia-web-${ganglia_web_version}"
  macro::download { "https://github.com/ganglia/ganglia-web/archive/${ganglia_web_version}.tar.gz":
    path    => $ganglia_web_tgz_file,
  } -> macro::extract { $ganglia_web_tgz_file :
    path    => '/usr/share',
    creates => $ganglia_web_dir,
  } -> file { '/usr/share/ganglia' :
    ensure => link,
    target => $ganglia_web_dir,
  } -> file { '/usr/share/ganglia/conf.php' :
    ensure => link,
    target => '/etc/ganglia/conf.php',
  }

  file { [ '/var/lib/ganglia-web',
           '/var/lib/ganglia-web/conf',
           '/var/lib/ganglia-web/dwoo',
           '/var/lib/ganglia-web/dwoo/cache',
           '/var/lib/ganglia-web/dwoo/compiled'
    ] :
    ensure => directory,
    owner => 'ganglia',
    group => 'apache',
    mode => 'ug=rwx,o=rx',
  }

  file { '/etc/httpd/conf.d/ganglia.conf' :
    ensure  => file,
    content => template('ganglia/httpd-ganglia.conf.erb'),
    require => [
      File['/usr/share/ganglia'],
      Package['httpd']
    ],
  }

  include ganglia::web::diskstat
  include ganglia::web::elasticsearch
}
