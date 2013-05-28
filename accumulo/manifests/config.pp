class accumulo::config ($javaHome, $user = 'accumulo', $group = 'hadoop') {

  exec { 'copy-example-config' :
    command => "/bin/cp /opt/accumulo-conf/examples/512MB/native-standalone/* /opt/accumulo-conf",
    user => "${user}",
    unless => "/usr/bin/test -f /opt/accumulo-conf/accumulo-env.sh"
  }

  find-and-replace { 'accumulo-env.sh JAVA_HOME' :
    file => '/opt/accumulo-conf/accumulo-env.sh',
    find => 'export JAVA_HOME=/path/to/java',
    replace => "export JAVA_HOME=${javaHome}",
    require => Exec['copy-example-config'],
  }

  find-and-replace { 'accumulo-env.sh HADOOP_HOME' :
    file => '/opt/accumulo-conf/accumulo-env.sh',
    find => 'export HADOOP_HOME=/path/to/hadoop',
    replace => 'export HADOOP_HOME=/opt/hadoop',
    require => Exec['copy-example-config'],
  }

  find-and-replace { 'accumulo-env.sh ZOOKEEPER_HOME' :
    file => '/opt/accumulo-conf/accumulo-env.sh',
    find => 'export ZOOKEEPER_HOME=/path/to/zookeeper',
    replace => 'export ZOOKEEPER_HOME=/opt/zookeeper',
    require => Exec['copy-example-config'],
  }

  find-and-replace { 'masters' :
    file => '/opt/accumulo-conf/masters',
    find => 'localhost',
    replace => "${ipaddress_eth1}",
    require => Exec['copy-example-config'],
  }

  find-and-replace { 'slaves' :
    file => '/opt/accumulo-conf/slaves',
    find => 'localhost',
    replace => "${ipaddress_eth1}",
    require => Exec['copy-example-config'],
  }

  exec { 'vm.swappiness=10 online' :
    command => '/sbin/sysctl -w vm.swappiness=10',
    unless => '/usr/bin/test $(/sbin/sysctl -n vm.swappiness) -eq 10',
  }

  exec { 'vm.swappiness=10 persistant' :
    command => '/bin/echo "vm.swappiness=10" >> /etc/sysctl.conf',
    unless => '/bin/grep -q vm.swappiness=10 /etc/sysctl.conf',
  }

  setup-passwordless-ssh { "${user}" :
  }

}
