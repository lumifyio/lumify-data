# == Class: ganglia::gmetad
#
# Main class used to deploy and setup the ganglia metadata collector component
#
# === Parameters
#
# [*ganglia_cluster_info*]
#   An array of hashes containing the cluster details to monitor (expected keys: name, polling_interval, machines).
#
class ganglia::gmetad (
  $ganglia_cluster_info = undef
) {

    include ganglia::gmetad::install, ganglia::gmetad::config, ganglia::gmetad::service

    Class['ganglia::gmetad::install'] -> Class['ganglia::gmetad::config'] -> Class['ganglia::gmetad::service']
}
