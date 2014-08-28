class ganglia::mon::rabbitmq {

  file { "/etc/ganglia/conf.d/rabbitmq.pyconf":
    ensure  => file,
    source => "puppet:///modules/ganglia/rabbitmq/conf.d/reabbitmq.pyconf",
  }

  file { "/usr/lib64/ganglia/python_modules/reabbitmq.py":
    ensure  => file,
    source => "puppet:///modules/ganglia/rabbitmq/python_modules/reabbitmq.py",
  }
}
