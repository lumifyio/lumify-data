class ganglia::mon::elasticsearch {

  file { "/etc/ganglia/conf.d/elasticsearch.pyconf":
    ensure  => file,
    source => "puppet:///modules/ganglia/elasticsearch/conf.d/elasticsearch.pyconf",
  }

  file { "/usr/share/ganglia/graph.d/es_report.json":
    ensure  => file,
    source => "puppet:///modules/ganglia/elasticsearch/graph.d/es_report.json",
  }

  file { "/usr/lib64/ganglia/python_modules/elasticsearch.py":
    ensure  => file,
    source => "puppet:///modules/ganglia/elasticsearch/python_modules/elasticsearch.py",
  }
}
