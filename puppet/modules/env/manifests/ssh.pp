class env::ssh {
  define setup($sshdir = $title, $user, $group, $env) {
    file { "${sshdir}/id_rsa":
      ensure => file,
      owner  => $user,
      group  => $group,
      source => "puppet:///modules/env/${env}/id_rsa",
      mode   => 0600,
    }

    file { "${sshdir}/id_rsa.pub":
      ensure => file,
      owner  => $user,
      group  => $group,
      source => "puppet:///modules/env/${env}/id_rsa.pub",
      mode   => 0644,
    }

    file { "${sshdir}/authorized_keys":
      ensure => file,
      owner  => $user,
      group  => $group,
      source => "puppet:///modules/env/${env}/authorized_keys",
      mode   => 0644,
    }

    file { "${sshdir}/known_hosts":
      ensure => file,
      owner  => $user,
      group  => $group,
      source => "puppet:///modules/env/${env}/known_hosts",
      mode   => 0644,
    }
  }
}