# https://spark.apache.org/docs/latest/security.html#configuring-ports-for-network-security

class spark::fw::master {
  firewall { '221 allow spark master' :
    port   => 7077,
    proto  => tcp,
    action => accept,
  }
  firewall { '222 allow spark master ui' :
    port   => 8080,
    proto  => tcp,
    action => accept,
  }
}

class spark::fw::worker {
  $spark_worker_port = hiera('spark_worker_port', '7078')
  firewall { '223 allow spark worker' :
    port   => $spark_worker_port,
    proto  => tcp,
    action => accept,
  }
  firewall { '224 allow spark worker ui' :
    port   => 8081,
    proto  => tcp,
    action => accept,
  }
}