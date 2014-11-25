class oozie($prefix = "/usr/lib/oozie") {
  require cloudera::cdh5::hadoop::base
  include macro

  $downloadpath = "/usr/local/src/ext-2.2.zip"

  package { "oozie":
    ensure  => installed,
    require => Package["hadoop-0.20"],
  }

  file { "oozie-extjs-dir":
    path    => "${prefix}/libext",
    ensure  => directory,
    require => Package["oozie"],
  }

  macro::download { "http://extjs.com/deploy/ext-2.2.zip":
    path    => $downloadpath,
  } -> macro::extract { $downloadpath:
    type    => "zip",
    path    => "${prefix}/libext",
    creates => "${prefix}/libext/ext-2.2",
    require => File["oozie-extjs-dir"],
  }

  file { '/etc/oozie/jobs' :
    ensure => directory,
    require => Package['oozie'],
  }

  $hadoop_masters = hiera_array('hadoop_masters')
  $hadoop_slaves = hiera_array('hadoop_slaves')
  $zookeeper_nodes = hiera_hash('zookeeper_nodes')
  $elasticsearch_locations = hiera_array('elasticsearch_locations')

  file { '/etc/oozie/jobs/job-common.properties' :
    ensure => file,
    content => template('oozie/job-common.properties.erb'),
    require => File['/etc/oozie/jobs'],
  }

  file { '/etc/oozie/jobs/job-common.xml' :
    ensure => file,
    content => template('oozie/job-common.xml.erb'),
    require => File['/etc/oozie/jobs'],
  }
}
