class ganglia::mon::elasticsearch {

  file { "/etc/ganglia/conf.d/elasticsearch.pyconf":
    ensure  => file,
    source => "puppet:///modules/ganglia/elasticsearch/conf.d/elasticsearch.pyconf",
  }

  file { "/usr/lib64/ganglia/python_modules/elasticsearch.py":
    ensure  => file,
    source => "puppet:///modules/ganglia/elasticsearch/python_modules/elasticsearch.py",
  }
}
