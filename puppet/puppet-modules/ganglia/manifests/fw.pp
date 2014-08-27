class ganglia::fw {
  firewall { '131 allow ganglia tcp' :
    proto  => tcp,
    port   => 8649,
    action => accept,
  }
  firewall { '132 allow ganglia udp' :
    proto  => udp,
    port   => 8649,
    action => accept,
  }
}
