class ganglia::mon::diskstat {
  Package['ganglia-gmond-python'] -> Class['ganglia::mon::diskstat']

  file { "/usr/lib64/ganglia/python_modules/diskstat.py":
    ensure  => file,
    source => "puppet:///modules/ganglia/diskstat/python_modules/diskstat.py",
  } -> file { "/etc/ganglia/conf.d/diskstat.pyconf":
    ensure  => file,
    source => "puppet:///modules/ganglia/diskstat/conf.d/diskstat.pyconf",
    notify => Service['gmond'],
  }
}
