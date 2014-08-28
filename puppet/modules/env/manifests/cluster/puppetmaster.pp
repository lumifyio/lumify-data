class puppetmaster::fw {
  firewall { '009 allow puppetmaster' :
    proto  => tcp,
    port   => 8140,
    action => accept,
  }
}

class ntpd::fw {
  firewall { '010 allow ntpd' :
    proto  => udp,
    port   => 123,
    action => accept,
  }
}

class env::cluster::puppetmaster {
  include '::ntp'

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
  class { 'ntpd::fw' :
    stage => 'first',
  }
  class { 'ganglia::fw' :
    stage => 'first',
  }

  include tinyproxy
  include ganglia::meta
  include ganglia::mon
  include ganglia::web

  tidy { '/var/lib/puppet/reports' :
    recurse => true,
    age => '3d',
  }
}
