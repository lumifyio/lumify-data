class macro {
  define download ($url = $title, $path) {
    $hiera_proxy_url = hiera('proxy_url', nil)

    if ($hiera_proxy_url != nil) {
      $curl_options = "${url} -s -L -o ${path} --proxy ${hiera_proxy_url}"
    } else {
      $curl_options = "${url} -s -L -o ${path}"
    }

    exec { "download-${url}" :
      cwd     => '/tmp',
      command => "/usr/bin/curl ${curl_options}",
      creates => $path,
      unless  => "/usr/bin/test -f ${path}",
    }
  }

  define extract ($file = $title, $type=undef, $user='root', $group='root', $path, $options='') {
    case $type {
      'zip':   { $cmd = "/usr/bin/unzip -qo ${options}" }
      'gzip':  { $cmd = "/bin/gunzip ${options}" }
      default: { $cmd = "/bin/tar ${options} -xzf" }
    }

    exec { "extract-${file}" :
      cwd     => $path,
      command => "${cmd} ${file}",
      user    => $user,
      group   => $group,
    }
  }

  define know-our-host-key ($user, $sshdir, $hostname) {
    exec { "know-our-host-key-${user}-${hostname}" :
      command => "/bin/echo \"${hostname} $(/bin/cat /etc/ssh/ssh_host_rsa_key.pub)\" >> ${sshdir}/known_hosts",
      user    => $user,
      unless  => "/bin/grep -q \"${hostname} $(/bin/cat /etc/ssh/ssh_host_rsa_key.pub)\" ${sshdir}/known_hosts",
    }
  }

  define setup-passwordless-ssh ($user = $title, $sshdir) {
    exec { "generate-ssh-keypair-${user}" :
      command => "/usr/bin/ssh-keygen -b 2048 -f ${sshdir}/id_rsa -N ''",
      user    => $user,
      creates => "${sshdir}/id_rsa",
    }

    exec { "authorize-ssh-key-${user}" :
      command => "/bin/cat ${sshdir}/id_rsa.pub >> ${sshdir}/authorized_keys",
      user    => $user,
      unless  => "/bin/grep -q \"$(/bin/cat ${sshdir}/id_rsa.pub)\" ${sshdir}/authorized_keys",
      require => Exec["generate-ssh-keypair-${user}"],
    }

    know-our-host-key { "${user}-localhost" :
      user     => $user,
      sshdir   => $sshdir,
      hostname => 'localhost',
      require  => Exec["generate-ssh-keypair-${user}"],
    }

    know-our-host-key { "${user}-${ipaddress_eth1}" :
      user     => $user,
      sshdir   => $sshdir,
      hostname => $ipaddress_eth1,
      require  => Exec["generate-ssh-keypair-${user}"],
    }
  }

  define git-clone ($url = $title, $path, $options = "", $timeout = 300) {
    $hiera_proxy_url = hiera('proxy_url', nil)
    if ($hiera_proxy_url != nil) {
      $git_environment = "http_proxy=${hiera_proxy_url}"
    } else {
      $git_environment = "proxy_is=not_available"
    }

    exec { "git clone ${options} ${url}" :
      command => "/usr/bin/git clone ${options} ${url} ${path}",
      environment => "${git_environment}",
      timeout => $timeout,
      creates => "${path}/.git",
      require => Package['git'],
    }
  }

  define git-checkout ($branch = $title, $path) {
    exec { "git checkout ${path} ${branch}" :
      command => "/usr/bin/git checkout ${branch}",
      cwd     => $path,
      unless  => "/usr/bin/git branch | /bin/grep '* ${branch}'",
      require => Package['git'],
    }
  }
}
