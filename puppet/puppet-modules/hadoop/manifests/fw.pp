class hadoop::fw::namenode (
  $srcnet = "0.0.0.0/0"
){
  firewall { '081 allow hadoop namenode' :
    port   => [8020, 40400, 50070],
    proto  => tcp,
    action => accept,
    source => "${srcnet}",
  }
}

class hadoop::fw::secondarynamenode (
  $srcnet = "0.0.0.0/0"
){
  firewall { '082 allow hadoop secondarynamenode' :
    port   => [50090, 56456],
    proto  => tcp,
    action => accept,
    source => "${srcnet}",
  }
}

class hadoop::fw::jobtracker (
  $srcnet = "0.0.0.0/0"
){
  firewall { '083 allow hadoop jobtracker' :
    port   => [8021, 37567, 50030],
    proto  => tcp,
    action => accept,
    source => "${srcnet}",
  }
}

class hadoop::fw::datanode (
  $srcnet = "0.0.0.0/0"
){
  firewall { '084 allow hadoop datanode' :
    port   => [50010, 50020, 50075, 51244],
    proto  => tcp,
    action => accept,
    source => "${srcnet}",
  }
}

class hadoop::fw::tasktracker (
  $srcnet = "0.0.0.0/0"
){
  firewall { '085 allow hadoop tasktracker' :
    port   => [34081, 50060],
    proto  => tcp,
    action => accept,
    source => "${srcnet}",
  }
}
