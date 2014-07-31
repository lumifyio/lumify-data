class macro::git {
  package { 'git' :
    ensure => present,
  }

  $hiera_proxy_url = hiera('proxy_url', nil)
  if ($hiera_proxy_url != nil) {
    exec { "git configure http.proxy as ${hiera_proxy_url}" :
      command     => "/usr/bin/git config --global --replace-all http.proxy ${hiera_proxy_url}",
      unless      => "/usr/bin/git config --global http.proxy | grep -q ${hiera_proxy_url}",
      environment => 'HOME=/root',
      require     => Package['git'],
      logoutput   => true,
    }
  } else {
    exec { 'git unconfigure http.proxy' :
      command     => '/usr/bin/git config --global --unset-all http.proxy',
      environment => 'HOME=/root',
      require     => Package['git'],
      returns     => [ 0, 5 ],
      logoutput   => on_failure,
    }
  }

  define clone ($url = $title, $path, $options = "", $timeout = 300) {
    exec { "git clone ${options} ${url}" :
      command     => "/bin/rm -rf ${path} && /usr/bin/git clone ${options} ${url} ${path} && /bin/touch ${path}/.done",
      environment => [ 'HOME=/root', 'GIT_CURL_VERBOSE=1' ],
      timeout     => $timeout,
      creates     => "${path}/.done",
      require     => Package['git'],
      logoutput   => on_failure,
    }
  }

  # TODO: avoid unnecessary execution
  define checkout ($branch = $title, $path) {
    exec { "git checkout ${path} ${branch}" :
      command => "/usr/bin/git checkout ${branch}",
      cwd     => $path,
      unless  => "/usr/bin/git branch | /bin/grep '* ${branch}'",
      require => Package['git'],
    }
  }
}
