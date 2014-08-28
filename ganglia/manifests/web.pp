# == Class: ganglia::web
#
# Main class used to deploy and setup the ganglia metadata collector web interface
#
# === Parameters
#
# [*ganglia_ip_addr*]
#   The ganglia source IP address. Defaults to '127.0.0.1'.
#
# [*ganglia_port*]
#   The ganglia source port number. Defaults to '8652'.
#
class ganglia::web (
  $ganglia_ip_addr = '127.0.0.1',
  $ganglia_port = '8652'
) {

  include ganglia::web::install, ganglia::web::config

  Class['ganglia::web::install'] -> Class['ganglia::web::config']

  # If running on a SELinux environment, create a port policy to allow access to the web port
  if $::selinux == 'true' {  # The selinux puppet fact is a string and is not converted to a boolean
    notify { 'Detected a SELinux environment, checking to see if policies need to be set': }

    package { 'policycoreutils-python' :
      ensure => installed,
    }

    exec { 'add SELinux policy exception for ganglia web port' :
      command => "/usr/sbin/semanage port -a -t http_port_t -p tcp ${ganglia_port}",
      unless  => "/usr/sbin/semanage port --list 2>/dev/null | /bin/egrep --quiet -s '^http_port_t\W+tcp\W+.*\W${ganglia_port}(\W|$)'",
      require => Package['policycoreutils-python'],
    }
  }
}
