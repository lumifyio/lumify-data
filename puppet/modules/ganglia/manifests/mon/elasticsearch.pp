class ganglia::mon::elasticsearch {
  Package['ganglia-gmond-python'] -> Class['ganglia::mon::elasticsearch']

  file { "/usr/lib64/ganglia/python_modules/elasticsearch.py":
    ensure  => file,
    source => "puppet:///modules/ganglia/elasticsearch/python_modules/elasticsearch.py",
  } -> file { "/etc/ganglia/conf.d/elasticsearch.pyconf":
    ensure  => file,
    source => "puppet:///modules/ganglia/elasticsearch/conf.d/elasticsearch.pyconf",
    notify => Service['gmond'],
  }
}
