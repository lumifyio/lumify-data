class ganglia::web::diskstat {
  File['/usr/share/ganglia'] -> Class['ganglia::web::diskstat']

  file { "/usr/share/ganglia/graph.d/diskstat_disktime_report.php":
    ensure  => file,
    source => "puppet:///modules/ganglia/diskstat/graph.d/diskstat_disktime_report.php",
  }
  file { "/usr/share/ganglia/graph.d/diskstat_iotime_report.php":
    ensure  => file,
    source => "puppet:///modules/ganglia/diskstat/graph.d/diskstat_iotime_report.php",
  }
  file { "/usr/share/ganglia/graph.d/diskstat_operations_report.php":
    ensure  => file,
    source => "puppet:///modules/ganglia/diskstat/graph.d/diskstat_operations_report.php",
  }
  file { "/usr/share/ganglia/graph.d/diskstat_readwritekb_report.php":
    ensure  => file,
    source => "puppet:///modules/ganglia/diskstat/graph.d/diskstat_readwritekb_report.php",
  }
}
