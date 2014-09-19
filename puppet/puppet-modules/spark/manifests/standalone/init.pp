# https://spark.apache.org/docs/latest/spark-standalone.html

class spark::standalone (
  $major_version = 'spark-1.1.0',
  $version = 'spark-1.1.0-bin-cdh4',
  $install_dir = '/opt',
  $user = 'spark',
  $group = 'hadoop'
) {
  require macro

  ensure_resource('group', $group, {'ensure' => 'present'})

  user { $user :
    ensure => present,
    groups => $group,
    home => "${install_dir}/spark",
  }

  $url = "http://www.us.apache.org/dist/spark/${major_version}/${version}.tgz"
  $tgz = "/tmp/${version}.tgz"
  macro::download { $url :
    path => $tgz,
  } -> macro::extract { $tgz :
    path    => $install_dir,
    creates => "${install_dir}/${version}",
  } -> file { "${install_dir}/${version}" :
    ensure => directory,
    owner => $user,
    group => $group,
    recurse => true,
    require => [ User[$user], Group[$group] ],
  } -> file { "${install_dir}/spark" :
    ensure => link,
    target => "${install_dir}/${version}",
    owner => $user,
    group => $group,
    require => [ User[$user], Group[$group] ],
  }

  $spark_master = "spark://${spark_master_hostname}:7077"
  $spark_driver_memory = hiera('spark_driver_memory', '5g')
  $spark_worker_port = hiera('spark_worker_port', '7078')

  file { "${install_dir}/spark/config/slaves" :
    content => template('spark/slaves.erb'),
    owner => $user,
    group => $group,
    require => [ File["${install_dir}/spark"], User[$user], Group[$group] ],
  }

  file { "${install_dir}/spark/config/spark-defaults.conf" :
    content => template('spark/spark-defaults.conf.erb'),
    owner => $user,
    group => $group,
    require => [ File["${install_dir}/spark"], User[$user], Group[$group] ],
  }

  file { "${install_dir}/spark/config/spark-env.sh" :
    content => template('spark/spark-env.sh.erb'),
    owner => $user,
    group => $group,
    mode => 'ug+x',
    require => [ File["${install_dir}/spark"], User[$user], Group[$group] ],
  }
}
