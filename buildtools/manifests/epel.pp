class buildtools::epel {

  exec { 'epel':
    command => '/bin/rpm -ivH http://dl.fedoraproject.org/pub/epel/6/x86_64/epel-release-6-8.noarch.rpm',
    unless => '/bin/rpm -q epel-release-6-8',
  }

}
