class env::dev {
  include role::hadoop::pseudo
  include ::zookeeper
  include role::accumulo::pseudo
  include role::elasticsearch::pseudo
  include ::ffmpeg
  include ::ccextractor
  include ::tesseract
  include ::opencv
  include role::oozie::pseudo
  include ::kafka
  include role::storm::master
  include role::storm::supervisor

  # TODO: configure firewall rules
  service { 'iptables' :
    enable => false,
    ensure => 'stopped',
  }

  file { '/opt/start.sh' :
    source => 'puppet:///modules/env/dev/start.sh',
    mode => 'u=rwx,g=,o=',
  }

  file { '/opt/stop.sh' :
    source => 'puppet:///modules/env/dev/stop.sh',
    mode => 'u=rwx,g=,o=',
  }

  file { '/opt/format.sh' :
    source => 'puppet:///modules/env/dev/format.sh',
    mode => 'u=rwx,g=,o=',
  }

  file { '/opt/shell.sh' :
    source => 'puppet:///modules/env/dev/shell.sh',
    mode => 'u=rwx,g=,o=',
  }

  file { '/opt/setup_oozie.sh' :
    source => 'puppet:///modules/env/dev/setup_oozie.sh',
    mode => 'u=rwx,g=,o=',
  }

  file { '/opt/run_oozie_workflow.sh' :
    source => 'puppet:///modules/env/dev/run_oozie_workflow.sh',
    mode => 'u=rwx,g=,o=',
  }

  file { '/opt/run_oozie_coordinator.sh' :
    source => 'puppet:///modules/env/dev/run_oozie_coordinator.sh',
    mode => 'u=rwx,g=,o=',
  }
}
