# == Class: ganglia
#
# Main class used to deploy and setup ganglia
#
class ganglia {
  $gangliaDir = '/etc/ganglia'

  $defaultPort = '8649'

  $udp_send_port = $defaultPort
  $udp_recv_port = $defaultPort
  $tcp_accept_port = $defaultPort

  package { 'ganglia' :
    ensure => present,
  }
}
