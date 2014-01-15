class clavin {
  include macro

  $tmp_file      = "/tmp/clavin-index.tgz"
  $index_archive = hiera('clavin_index_archive')
  $index_dir     = hiera('clavin_index_dir')

  file { $index_dir :
    ensure => directory,
  }

  macro::download { $index_archive :
    path => $tmp_file,
  } -> macro::extract { $tmp_file :
    path => $index_dir,
    creates => "${index_dir}/segments.gen",
    require => File[$index_dir],
  }
}
