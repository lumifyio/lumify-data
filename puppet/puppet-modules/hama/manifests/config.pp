class hama::config ($javaHome, $user = 'hama', $group = 'hadoop') {

  find-and-replace { 'hama-env.sh JAVA_HOME' :
    file => '/opt/hama-conf/hama-env.sh',
    find => '#export JAVA_HOME=/usr/lib/jvm/java-6-sun',
    replace => "export JAVA_HOME=${javaHome}",
  }

  find-and-replace { 'hama-env.sh HAMA_MANAGES_ZK' :
    file => '/opt/hama-conf/hama-env.sh',
    find => '# export HAMA_MANAGES_ZK=true',
    replace => 'export HAMA_MANAGES_ZK=false',
  }

  setup-passwordless-ssh { "${user}" :
  }

}
