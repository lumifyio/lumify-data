class zookeeper::fw::node (
  $srcnet = "0.0.0.0/0"
){
  firewall { '260 allow zookeeper' :
    port   => [2181, 2888, 3888],
    proto  => tcp,
    action => accept,
    source => "${srcnet}",
  }
}
