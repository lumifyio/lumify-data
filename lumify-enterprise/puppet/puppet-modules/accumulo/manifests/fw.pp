class accumulo::fw::logger {
  firewall { '011 allow accumulo logger' :
    port   => [11224],
    proto  => tcp,
    action => accept,
  }
}

class accumulo::fw::tserver {
  firewall { '012 allow accumulo tserver' :
    port   => [9997],
    proto  => tcp,
    action => accept,
  }
}

class accumulo::fw::master {
  firewall { '013 allow accumulo master' :
    port   => [9999],
    proto  => tcp,
    action => accept,
  }
}

class accumulo::fw::gc {
  firewall { '014 allow accumulo gc' :
    port   => [50091],
    proto  => tcp,
    action => accept,
  }
}

class accumulo::fw::monitor {
  firewall { '015 allow accumulo monitor' :
    port   => [4560, 50095],
    proto  => tcp,
    action => accept,
  }
}

class accumulo::fw::tracer {
  firewall { '016 allow accumulo tracer' :
    port   => [12234],
    proto  => tcp,
    action => accept,
  }
}
