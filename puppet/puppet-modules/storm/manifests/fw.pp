class storm::fw::nimbus (
  $srcnet = "0.0.0.0/0"
){
  $storm_nimbus_thrift_port = hiera('storm_nimbus_thrift_port')
  firewall { '191 allow storm nimbus' :
    port   => $storm_nimbus_thrift_port,
    proto  => tcp,
    action => accept,
    source => "${srcnet}",
  }
}

class storm::fw::ui (
  $srcnet = "0.0.0.0/0"
){
  $storm_ui_port = hiera('storm_ui_port')
  firewall { '192 allow storm ui' :
    port   => $storm_ui_port,
    proto  => tcp,
    action => accept,
    source => "${srcnet}",
  }
}

class storm::fw::supervisor (
  $srcnet = "0.0.0.0/0"
){
  $storm_supervisor_slots_ports = hiera_array('storm_supervisor_slots_ports')
  firewall { '193 allow storm supervisor' :
    port   => $storm_supervisor_slots_ports,
    proto  => tcp,
    action => accept,
    source => "${srcnet}",
  }

  $storm_supervisor_jmx_registry_ports = hiera_array('storm_supervisor_jmx_registry_ports')
  firewall { '194 allow storm supervisor jmx registry' :
    port   => $storm_supervisor_jmx_registry_ports,
    proto  => tcp,
    action => accept,
    source => "${srcnet}",
  }

  $storm_supervisor_jmx_objects_ports = hiera_array('storm_supervisor_jmx_objects_ports')
  firewall { '195 allow storm supervisor jmx objects' :
    port   => $storm_supervisor_jmx_objects_ports,
    proto  => tcp,
    action => accept,
    source => "${srcnet}",
  }
}

class storm::fw::logviewer (
  $srcnet = "0.0.0.0/0"
){
  firewall { '196 allow storm logviewers' :
    port   => 8000,
    proto  => tcp,
    action => accept,
    source => "${srcnet}",
  }
}
