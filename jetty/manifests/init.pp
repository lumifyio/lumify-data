class jetty(
  $major_version="9",
  $version="9.0.5.v20130815"
){

  macro::download { 'jetty-download':
    url  => "http://eclipse.org/downloads/download.php?file=/jetty/stable-${major_version}/dist/jetty-distribution-${version}.tar.gz&r=1",
    path => "/tmp/jetty-distribution-${version}.tar.gz",
  } -> macro::extract { 'jetty-extract':
    file => "/tmp/jetty-distribution-${version}.tar.gz",
    path => "/opt",
  }

  file { "/opt/jetty" :
    ensure  => link,
    target  => "/opt/jetty-distribution-${version}",
    require => Macro::Extract['jetty-extract'],
  }

}
