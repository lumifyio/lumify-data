class env::cluster::puppetmaster {
  class { buildtools::epel :
    proxy_url => 'disabled',
  }

  include my_fw
  class { 'tinyproxy::fw' :
    stage => 'first',
  }

  firewall { '009 allow puppetmaster' :
    proto  => tcp,
    port   => 8140,
    action => accept,
  }

  include tinyproxy
}
