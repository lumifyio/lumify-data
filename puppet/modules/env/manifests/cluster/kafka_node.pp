class env::cluster::kafka_node inherits env::cluster::base {
  include my_fw
  class { 'kafka::fw::node' :
    stage => 'first',
  }

  include role::kafka::node

  $hiera_proxy_url = hiera('proxy_url', nil)
  file { '/opt/kafka/proxy_java_opts' :
    ensure  => file,
    content => template('env/cluster/proxy_java_opts.erb'),
    require => File['/opt/kafka'],
  }

  exec { 'patch sbt with proxy java opts' :
    command => '/bin/sed -i -e "s/^java/java $(/bin/cat /opt/kafka/proxy_java_opts)/" /opt/kafka/sbt',
    unless  => '/bin/grep -q -f /opt/kafka/proxy_java_opts /opt/kafka/sbt',
    require => [ File['/opt/kafka/proxy_java_opts'],
                 Macro::Extract["${kafka::downloadpath}"],
               ],
    before  => Exec[ 'sbt update',
                     'sbt package'
                   ],
  }

  file { '/opt/lumify/kafka-clear.sh' :
    source => 'puppet:///modules/env/common/kafka-clear.sh',
    owner => 'root',
    mode => 'u=rwx,g=,o=',
    require => File['/opt/lumify'],
  }

}
