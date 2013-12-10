class env::cluster::node {
  include hadoop_slave
  class { 'accumulo_node' :
    stage => 'first',
  }
  class { 'elasticsearch_node' :
    stage => 'first',
  }

  file { '/etc/yum.repos.d/lumify.repo' :
    source => 'puppet:///modules/env/dev/lumify.repo',
    owner => 'root',
    mode => 'u=rw,g=r,o=r',
  }

  package { [ 'lumify-ffmpeg', 'lumify-ccextractor', 'lumify-tesseract', 'lumify-tesseract-eng', 'lumify-opencv' ] :
    ensure => present,
    require => File['/etc/yum.repos.d/lumify.repo'],
  }
}
