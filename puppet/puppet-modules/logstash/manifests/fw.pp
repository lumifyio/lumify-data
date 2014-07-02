class logstash::fw::ui {
  firewall { '121 allow kibna ui for logstash' :
    proto  => tcp,
    port   => 9292,
    action => accept,
  }
}
