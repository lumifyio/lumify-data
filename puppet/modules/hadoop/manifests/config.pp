class hadoop::config ($javaHome, $user = 'hadoop', $group = 'hadoop') {

  file { [ '/var/lib/hadoop-0.20', '/var/lib/hadoop-0.20/cache' ] :
    ensure => 'directory',
    owner => "${user}",
    group => "${group}",
    mode => 'u=rwx,g=rwxs,o=',
    require => [ User["${user}"], Group["${group}"] ],
  }

  file { '/opt/hadoop-conf/core-site.xml' :
    source => 'puppet:///modules/hadoop/core-site.xml',
    backup => '.DIST',
  }

  file { '/opt/hadoop-conf/hdfs-site.xml' :
    source => 'puppet:///modules/hadoop/hdfs-site.xml',
    backup => '.DIST',
  }

  file { '/opt/hadoop-conf/mapred-site.xml' :
    source => 'puppet:///modules/hadoop/mapred-site.xml',
    backup => '.DIST',
  }

  find-and-replace { 'hadoop-env.sh JAVA_HOME' :
    file => '/opt/hadoop-conf/hadoop-env.sh',
    find => '# export JAVA_HOME=/usr/lib/j2sdk1.6-sun',
    replace => "export JAVA_HOME=${javaHome}",
  }

  setup-passwordless-ssh { "${user}" :
  }

}
