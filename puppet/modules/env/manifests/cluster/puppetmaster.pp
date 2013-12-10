class puppetmaster::fw {
  firewall { '009 allow puppetmaster' :
    proto  => tcp,
    port   => 8140,
    action => accept,
  }
}

class env::cluster::puppetmaster {
  class { buildtools::epel :
    proxy_url => 'disabled',
  }

  include my_fw
  class { 'tinyproxy::fw' :
    stage => 'first',
  }
  class { 'puppetmaster::fw' :
    stage => 'first',
  }

  include tinyproxy
}
