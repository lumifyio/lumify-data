# == Class: ganglia::gmond
#
# Main class used to deploy and setup the ganglia monitor component
#
# === Parameters
#
# [*ganglia_cluster_name*]
#   The cluster name used to associate monitor daemons together.  Defaults to 'Unknown'.
#
# [*ganglia_udp_send_channel_info*]
#   An array of hashes containing the monitoring UDP send channel info (expected keys: host, port). Not required.
#
# [*ganglia_udp_recv_channel_info*]
#   An array of hashes containing the monitoring UDP recv channel info (expected keys: port). Not required.
#
# [*ganglia_tcp_accept_channel_info*]
#   An array of hashes containing the monitoring TCP accept channel info (expected keys: port). Not required.
#
class ganglia::gmond (
  $ganglia_cluster_name = 'Unknown',
  $ganglia_udp_send_channel_info = undef,
  $ganglia_udp_recv_channel_info = undef,
  $ganglia_tcp_accept_channel_info = undef
) {

    include ganglia::gmond::install, ganglia::gmond::config, ganglia::gmond::service

    Class['ganglia::gmond::install'] -> Class['ganglia::gmond::config'] -> Class['ganglia::gmond::service']
}
