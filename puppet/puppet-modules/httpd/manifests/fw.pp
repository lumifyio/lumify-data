class httpd::fw::http {
  firewall { '400 allow httpd standard' :
    port   => [80],
    proto  => tcp,
    action => accept,
  }
}

class httpd::fw::https {
  firewall { '401 allow httpd ssl' :
    port   => [443],
    proto  => tcp,
    action => accept,
  }
}
