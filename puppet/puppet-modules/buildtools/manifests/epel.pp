class buildtools::epel($proxy_url = nil) {
  case $proxy_url {
    'disabled' : {
                    $rpm_environment = "proxy_was=disabled"
                 }
    nil        : {
                    $hiera_proxy_url = hiera('proxy_url', nil)
                    if ($hiera_proxy_url != nil) {
                      $rpm_environment = "http_proxy=${hiera_proxy_url}"
                    } else {
                      $rpm_environment = "proxy_was=not_set_and_was_unavailable"
                    }
                 }
    default    : {
                    $rpm_environment = "http_proxy=${proxy_url}"
                 }
  }

  exec { 'epel':
    command => '/bin/rpm -ivH http://dl.fedoraproject.org/pub/epel/6/x86_64/epel-release-6-8.noarch.rpm',
    environment => "${rpm_environment}",
    unless => '/bin/rpm -q epel-release-6-8',
  }

  if $rpm_environment =~ /^http_proxy=/ {
    exec { 'epel-disable-mirrorlist' :
      command => "/bin/sed -i -e 's/mirrorlist=/#mirrorlist=/' -e 's/#baseurl=/baseurl=/' /etc/yum.repos.d/epel*.repo",
      require => Exec['epel'],
    }
  }
}
