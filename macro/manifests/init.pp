class macro {
  define download ($url = $title, $path) {
    exec { "download-${url}" :
      cwd     => '/opt',
      command => "/usr/bin/curl ${url} -s -L -o ${path}",
      creates => $path,
      unless => "/usr/bin/test -f ${path}",
    }
  }

  define extract ($file = $title, $type=undef, $user='root', $group='root', $path) {
    case $type {
      'zip':   { $cmd = '/usr/bin/unzip -q' }
      'gzip':  { $cmd = '/bin/gunzip' }
      default: { $cmd = '/bin/tar xzf' }
    }

    exec { "extract-${file}" :
      cwd     => $path,
      command => "${cmd} ${file}",
      user    => $user,
      group   => $group,
    }
  }
}