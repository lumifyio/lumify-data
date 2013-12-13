# NOTE: declare the following 'first' stage in your site.pp file
#
# stage { 'first' :
#   before => Stage['main'],
# }

class my_fw {
}

resources { 'firewall' :
  purge => true,
}

Firewall {
  before  => Class['my_fw::post'],
  require => Class['my_fw::pre'],
}

class { ['my_fw::pre', 'my_fw::post'] :
  stage => 'first',
}

class { 'firewall' :
  stage => 'first',
}
