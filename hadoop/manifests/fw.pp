class hadoop::fw::namenode {
  firewall { '081 allow hadoop namenode' :
    port   => [8020, 40400, 50070],
    proto  => tcp,
    action => accept,
  }
}

class hadoop::fw::secondarynamenode {
  firewall { '082 allow hadoop secondarynamenode' :
    port   => [50090, 56456],
    proto  => tcp,
    action => accept,
  }
}

class hadoop::fw::jobtracker {
  firewall { '083 allow hadoop jobtracker' :
    port   => [8021, 37567, 50030],
    proto  => tcp,
    action => accept,
  }
}

class hadoop::fw::datanode {
  firewall { '084 allow hadoop datanode' :
    port   => [50010, 50020, 50075, 51244],
    proto  => tcp,
    action => accept,
  }
}

class hadoop::fw::tasktracker {
  firewall { '085 allow hadoop tasktracker' :
    port   => [34081, 50060],
    proto  => tcp,
    action => accept,
  }
}
