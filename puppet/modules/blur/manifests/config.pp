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

  setup-passwordless-ssh { "${user}" :
  }

}
