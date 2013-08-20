class jetty::fw::server {
  firewall { '101 allow http and https for jetty' :
    proto  => tcp,
    port   => [80, 443],
    action => allow,
  }

  firewall { '102 map 80 to 8080 for jetty' :
    table  => 'nat',
    chail  => 'PREROUTING',
    proto  => tcp,
    port   => 80,
    jump   => 'REDIRECT',
    toport => 8080,
  }

  firewall { '103 map 443 to 8443 for jetty' :
    table  => 'nat',
    chail  => 'PREROUTING',
    proto  => tcp,
    port   => 443,
    jump   => 'REDIRECT',
    toport => 8443,
  }
}
