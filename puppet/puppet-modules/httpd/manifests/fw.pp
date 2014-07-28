class httpd::fw::http (
  $srcnet = "0.0.0.0/0"
){
  firewall { '400 allow httpd standard' :
    port   => [80],
    proto  => tcp,
    action => accept,
    source => "${srcnet}",
  }
}

class httpd::fw::https (
  $srcnet = "0.0.0.0/0"
){
  firewall { '401 allow httpd ssl' :
    port   => [443],
    proto  => tcp,
    action => accept,
    source => "${srcnet}",
  }
}
