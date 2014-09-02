class ganglia::mon::rabbitmq {
  Package['ganglia-gmond-python'] -> Class['ganglia::mon::rabbitmq']

  file { "/usr/lib64/ganglia/python_modules/rabbitmq.py":
    ensure  => file,
    source => "puppet:///modules/ganglia/rabbitmq/python_modules/rabbitmq.py",
  } -> file { "/etc/ganglia/conf.d/rabbitmq.pyconf":
    ensure  => file,
    source => "puppet:///modules/ganglia/rabbitmq/conf.d/rabbitmq.pyconf",
    notify => Service['gmond'],
  }
}
