class tomcat::fw::http {
  firewall { '450 allow tomcat standard' :
    port   => [8080],
    proto  => tcp,
    action => accept,
  }
}

class tomcat::fw::https {
  firewall { '451 allow tomcat ssl' :
    port   => [8443],
    proto  => tcp,
    action => accept,
  }
}

class tomcat::fw::ajp {
  firewall { '452 allow tomcat AJP' :
    port   => [8009],
    proto  => tcp,
    action => accept,
  }
}
