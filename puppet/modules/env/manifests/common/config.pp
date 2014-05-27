class env::common::config(
  $main_properties_filename = 'lumify.properties'
) {
  file { [ '/opt', '/opt/lumify', '/opt/lumify/config' ] :
    ensure => directory,
  }

  group { 'lumify' :
    ensure => present,
    name => 'lumify',
  }

  define add_user_to_group (
    $group
  ) {
    exec { "group-${group}-add-${name}" :
      command => "/usr/sbin/usermod -a -G ${group} ${name}",
      onlyif  => "/usr/bin/id ${name}",
      unless  => "/usr/bin/groups ${name} | /bin/grep -q ${group}",
      returns => [ 0, 6 ],
      require => Group[$group],
    }
  }

  add_user_to_group { [
      'jetty',
      'tomcat',
      'storm',
      'hdfs',
      'mapred'
    ] :
    group => 'lumify',
  }

  if $hostname =~ /lumify-vm/ {
    add_user_to_group { 'vagrant' : group => 'lumify' }
  }

  file { '/opt/lumify/logs' :
    ensure => directory,
    group => 'lumify',
    mode => 'u=rwx,g=rwxs,o=rx',
    require => [ File['/opt/lumify'], Group['lumify'] ],
  }

  $data_dir_list = split($data_directories, ',')
  $first_data_dir = $data_dir_list[0]

  file { "${first_data_dir}/hdfslibcache" :
    ensure => directory,
    group => 'lumify',
    mode => 'u=rwx,g=rwxs,o=rx',
    require => [ File[$first_data_dir], Group['lumify'] ],
  }

  $syslog_server = hiera('syslog_server', '')
  $syslog_facility = 'local3'
  $syslog_threshold = hiera('syslog_threshold', 'ERROR')
  $hadoop_masters = hiera_array('hadoop_masters')
  $hadoop_slaves = hiera_array('hadoop_slaves')
  $zookeeper_nodes = hiera_hash('zookeeper_nodes')
  $accumulo_masters = hiera_array('accumulo_masters')
  $accumulo_slaves = hiera_array('accumulo_slaves')
  $elasticsearch_locations = hiera_array('elasticsearch_locations')
  $rabbitmq_nodes = hiera_array('rabbitmq_nodes')
  $storm_supervisor_slots_ports = hiera_array('storm_supervisor_slots_ports')
  $clavin_index_dir = hiera('clavin_index_dir')

  define config_file {
    file { "/opt/lumify/config/${name}" :
      ensure => file,
      content => template("env/common/${name}.erb"),
    }
  }

  config_file { [
      'log4j.xml',
      $main_properties_filename,
      'lumify-enterprise.properties',
      'lumify-clavin.properties'
    ] :
    require => File['/opt/lumify/config'],
  }
}
