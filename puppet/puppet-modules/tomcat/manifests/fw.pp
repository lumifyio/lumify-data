class tomcat::fw::http {
  $tomcat_http_port=hiera("tomcat_http_port",8080)
  firewall { '450 allow tomcat standard' :
    port   => [$tomcat_http_port],
    proto  => tcp,
    action => accept,
  }
}

class tomcat::fw::https {
  $tomcat_https_port=hiera("tomcat_https_port",8443)
  firewall { '451 allow tomcat ssl' :
    port   => [$tomcat_https_port],
    proto  => tcp,
    action => accept,
  }
}

class tomcat::fw::ajp {
  $mod_jk_workers = hiera_hash("mod_jk_workers")
  $worker_defaults = {
    port => 8009
  }

  create_resources(apply_ajp_fw_rule, $mod_jk_workers, $worker_defaults)

}

define apply_ajp_fw_rule ($host, $port) {
  notice "${name}: ${host}, ${port}"
  if $interfaces =~ /eth1/ {
    $worker_ip = $ipaddress_eth1
  } elsif $interfaces =~ /em2/ {
    $worker_ip = $ipaddress_em2
  } else {
    $worker_ip = $ipaddress_eth0
  }

  if $host == $worker_ip {
    firewall { '452 allow tomcat AJP' :
      port   => [$port],
      proto  => tcp,
      action => accept,
    }
  }
}
