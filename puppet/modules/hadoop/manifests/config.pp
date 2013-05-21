class hadoop::config ($javaHome, $user = 'hadoop', $group = 'hadoop') {

  file { [ '/var/lib/hadoop-0.20', '/var/lib/hadoop-0.20/cache' ] :
    ensure => 'directory',
    owner => 'hadoop',
    group => 'hadoop',
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

  define find-and-replace ($file = $title, $find, $replace) {
    exec { "find-and-replace-${file}-${find}" :
      command => "/bin/sed -i.DIST -e 's|${find}|${replace}|' ${file}",
      unless => "/bin/grep -q '${replace}' ${file}",
    }
  }

  find-and-replace { '/opt/hadoop-conf/hadoop-env.sh' :
    find => '# export JAVA_HOME=/usr/lib/j2sdk1.6-sun',
    replace => "export JAVA_HOME=${javaHome}",
  }

}
