class blur::config ($javaHome, $user = 'blur', $group = 'hadoop') {

  find-and-replace { 'blur-env.sh JAVA_HOME' :
    file => '/opt/blur-conf/blur-env.sh',
    find => '# export JAVA_HOME=/usr/lib/j2sdk1.6-sun',
    replace => "export JAVA_HOME=${javaHome}",
  }

  find-and-replace { 'blur-env.sh HADOOP_HOME' :
    file => '/opt/blur-conf/blur-env.sh',
    find => '# export HADOOP_HOME=/var/hadoop-0.20.2',
    replace => "export HADOOP_HOME=/opt/hadoop",
  }

  find-and-replace { 'blur-env.sh -XX:OnOutOfMemoryError' :
    file => '/opt/blur-conf/blur-env.sh',
    find => ' -XX:OnOutOfMemoryError',
    replace => '\" # -XX:OnOutOfMemoryError',
  }

  find-and-replace { 'blur-env.sh BLUR_MANAGE_ZK' :
    file => '/opt/blur-conf/blur-env.sh',
    find => 'export BLUR_MANAGE_ZK=true',
    replace => 'export BLUR_MANAGE_ZK=false',
  }

  exec { 'ulimit -Sn 50000' :
    command => '/bin/echo "* soft nofile 50000" >> /etc/security/limits.conf',
    unless => '/bin/grep -q "* soft nofile 50000" /etc/security/limits.conf',
  }

  exec { 'ulimit -Hn 100000' :
    command => '/bin/echo "* hard nofile 100000" >> /etc/security/limits.conf',
    unless => '/bin/grep -q "* hard nofile 100000" /etc/security/limits.conf',
  }

  setup-passwordless-ssh { "${user}" :
  }

  file { '/opt/blur/logs' :
    ensure => directory,
    owner => "${user}",
    group => "${group}"
  }

  file { '/opt/blur/pids' :
    ensure => directory,
    owner => "${user}",
    group => "${group}"
  }

}
