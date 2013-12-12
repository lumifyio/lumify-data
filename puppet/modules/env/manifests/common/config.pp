class env::common::config {
  file { [ '/opt', '/opt/lumify', '/opt/lumify/config' ] :
    ensure => directory,
  }

  file { '/opt/lumify/config/log4j.xml' :
    ensure => file,
    source => 'puppet:///modules/env/cluster/log4j.xml',
    require => File['/opt/lumify/config'],
  }

  file { '/opt/lumify/config/configuration.properties' :
    ensure => file,
    content => template('env/cluster/configuration.properties.erb'),
    require => File['/opt/lumify/config'],
  }

  file { '/opt/lumify/config/credentials.properties-EXAMPLE' :
    ensure => file,
    source => 'puppet:///modules/env/cluster/credentials.properties-EXAMPLE',
    require => File['/opt/lumify/config'],
  }
}
