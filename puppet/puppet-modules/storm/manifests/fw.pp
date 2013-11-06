class storm::fw::nimbus {
  $storm_nimbus_thrift_port = hiera('storm_nimbus_thrift_port')
  firewall { '191 allow storm nimbus' :
    port   => $storm_nimbus_thrift_port,
    proto  => tcp,
    action => accept,
  }
}

class storm::fw::ui {
  $storm_ui_port = hiera('storm_ui_port')
  firewall { '192 allow storm ui' :
    port   => $storm_ui_port,
    proto  => tcp,
    action => accept,
  }
}

class storm::fw::supervisor {
  $storm_supervisor_slots_ports = hiera_array('storm_supervisor_slots_ports')
  firewall { '193 allow storm supervisor' :
    port   => $storm_supervisor_slots_ports,
    proto  => tcp,
    action => accept,
  }
}