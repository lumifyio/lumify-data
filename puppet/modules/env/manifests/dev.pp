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
    owner => 'vagrant',
    mode => 'u=rwx,g=,o=',
  }

  file { '/opt/status.sh' :
    source => 'puppet:///modules/env/dev/status.sh',
    owner => 'vagrant',
    mode => 'u=rwx,g=,o=',
  }

  file { '/opt/stop.sh' :
    source => 'puppet:///modules/env/dev/stop.sh',
    owner => 'vagrant',
    mode => 'u=rwx,g=,o=',
  }

  file { '/opt/format.sh' :
    source => 'puppet:///modules/env/dev/format.sh',
    owner => 'vagrant',
    mode => 'u=rwx,g=,o=',
  }

  file { '/opt/shell.sh' :
    source => 'puppet:///modules/env/dev/shell.sh',
    owner => 'vagrant',
    mode => 'u=rwx,g=,o=',
  }

  file { '/opt/setup_oozie.sh' :
    source => 'puppet:///modules/env/dev/setup_oozie.sh',
    owner => 'vagrant',
    mode => 'u=rwx,g=,o=',
  }

  file { '/opt/run_oozie_workflow.sh' :
    source => 'puppet:///modules/env/dev/run_oozie_workflow.sh',
    owner => 'vagrant',
    mode => 'u=rwx,g=,o=',
  }

  file { '/opt/run_oozie_coordinator.sh' :
    source => 'puppet:///modules/env/dev/run_oozie_coordinator.sh',
    owner => 'vagrant',
    mode => 'u=rwx,g=,o=',
  }

  file { '/opt/storm-kill.sh' :
    source => 'puppet:///modules/env/dev/storm-kill.sh',
    owner => 'vagrant',
    mode => 'u=rwx,g=,o=',
  }

  file { '/opt/storm-run.sh' :
    source => 'puppet:///modules/env/dev/storm-run.sh',
    owner => 'vagrant',
    mode => 'u=rwx,g=,o=',
  }

  file { '/opt/kafka-clear.sh' :
    source => 'puppet:///modules/env/dev/kafka-clear.sh',
    owner => 'vagrant',
    mode => 'u=rwx,g=,o=',
  }
}
