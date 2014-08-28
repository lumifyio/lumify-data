class ganglia::mon::diskstat {

  file { "/etc/ganglia/conf.d/diskstat.pyconf":
    ensure  => file,
    source => "puppet:///modules/ganglia/diskstat/conf.d/diskstat.pyconf",
  }

  file { "/usr/lib64/ganglia/python_modules/diskstat.py":
    ensure  => file,
    source => "puppet:///modules/ganglia/diskstat/python_modules/diskstat.py",
  }
}
