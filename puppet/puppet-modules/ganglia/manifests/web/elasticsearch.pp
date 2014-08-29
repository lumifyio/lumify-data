class ganglia::web::elasticsearch {
  File['/usr/share/ganglia'] -> Class['ganglia::web::elasticsearch']

  file { "/usr/share/ganglia/graph.d/es_report.json":
    ensure  => file,
    source => "puppet:///modules/ganglia/elasticsearch/graph.d/es_report.json",
  }
}
