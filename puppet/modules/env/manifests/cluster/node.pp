class env::cluster::node {
  include hadoop_slave
  include accumulo_node
  include elasticsearch_node
  include clavin
  include storm_supervisor

  file { '/etc/yum.repos.d/lumify.repo' :
    source => 'puppet:///modules/env/dev/lumify.repo',
    owner => 'root',
    mode => 'u=rw,g=r,o=r',
  }

  package { [ 'lumify-ffmpeg',
              'lumify-ccextractor',
              'lumify-tesseract',
              'lumify-tesseract-eng',
              'lumify-opencv',
              'lumify-pocketsphinx',
            ] :
    ensure => present,
    require => File['/etc/yum.repos.d/lumify.repo'],
  }

  # install sox after lumify-ffmpeg because of conflicting libs
  package { 'sox' :
    ensure => present,
    require => Package['lumify-ffmpeg'],
  }
}
