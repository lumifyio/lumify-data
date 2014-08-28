# == Class: ganglia::gmond::fw
#
# Responsible for ganglia monitor component firewall configuration
#
# === Parameters
#
# [*srcnet*]
#   The source network in which to apply the firewall rule.  Defaults to 0.0.0.0/0.
#
# [*tcp_accept_port*]
#   The TCP port ganglia monitor uses to publish metrics to ganglia metadata. Defaults to 8649.
#
# [*udp_recv_send_port*]
#   The UDP port ganglia monitor uses to receive and publish metrics. Defaults to 8649.
#
class ganglia::gmond::fw (
  $srcnet = '0.0.0.0/0',
  $tcp_accept_port = '8649',
  $udp_recv_send_port = '8649'
) {

  firewall { '201 allow ganglia monitor daemon receive' :
    port   => [$tcp_accept_port],
    proto  => tcp,
    action => accept,
    source => $srcnet,
  }

  firewall { '202 allow ganglia monitor daemon send/receive' :
    port   => [$udp_recv_send_port],
    proto  => udp,
    action => accept,
    source => $srcnet,
  }
}
