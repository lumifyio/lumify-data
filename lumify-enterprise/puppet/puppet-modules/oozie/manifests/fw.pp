class oozie::fw::server {
  firewall { '161 allow oozie server' :
    port   => [11000],
    proto  => tcp,
    action => accept,
  }
}
