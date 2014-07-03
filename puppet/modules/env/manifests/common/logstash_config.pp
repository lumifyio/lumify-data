class env::common::logstash_config {
  $logstash_server = hiera('logstash_server', '')

  if $logstash_server != '' {
    file { '/opt/logstash/logstash.config' :
      ensure  => file,
      content => template('env/common/logstash.config.erb'),
    }
  }
}
