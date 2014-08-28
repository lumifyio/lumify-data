class ganglia::mon::diskstat {

  file { "/etc/ganglia/conf.d/diskstat.pyconf":
    ensure  => file,
    source => "puppet:///modules/ganglia/diskstat/conf.d/diskstat.pyconf",
  }

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

  file { "/usr/lib64/ganglia/python_modules/diskstat.py":
    ensure  => file,
    source => "puppet:///modules/ganglia/diskstat/python_modules/diskstat.py",
  }
}
