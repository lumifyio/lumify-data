class jetty::fw::server {
  firewall { '101 allow 8080 and 8443 for jetty' :
    proto  => tcp,
    port   => [8080, 8443],
    action => accept,
  }

  firewall { '102 map 80 to 8080 for jetty' :
    table   => 'nat',
    chain   => 'PREROUTING',
    proto   => tcp,
    port    => 80,
    jump    => 'REDIRECT',
    toports => 8080,
  }

  firewall { '103 map 443 to 8443 for jetty' :
    table   => 'nat',
    chain   => 'PREROUTING',
    proto   => tcp,
    port    => 443,
    jump    => 'REDIRECT',
    toports => 8443,
  }
}
