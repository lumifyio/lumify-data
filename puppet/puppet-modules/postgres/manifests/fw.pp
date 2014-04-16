class postgres::fw::db (
  $srcnet = "0.0.0.0/0" ){
    
  firewall { '520 allow postgres standard' :
    port   => [5432],
    proto  => tcp,
    action => accept,
    source => "${srcnet}"
  }
  
}
