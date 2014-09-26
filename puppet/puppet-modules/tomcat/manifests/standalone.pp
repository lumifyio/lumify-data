class tomcat::standalone {
  include tomcat

  define tomcat_server (
    $tomcat_id = $title,
    $tomcat_shutdown_port = hiera('tomcat_shutdown_port', 8005),
    $tomcat_http_port = hiera('tomcat_http_port', 8080),
    $tomcat_https_port = hiera('tomcat_https_port',8443),
    $tomcat_keystore_path = hiera('tomcat_keystore_path'),
    $tomcat_keystore_password = hiera('tomcat_keystore_password'),
    $tomcat_truststore_path = hiera('tomcat_truststore_path'),
    $tomcat_truststore_password = hiera('tomcat_truststore_password'),
  ) {
    $catalina_home = "/opt/${tomcat_id}"

    extract_tomcat { $catalina_home : }

    file { "${catalina_home}/conf/server.xml" :
      ensure  => file,
      content => template('tomcat/server.xml.standalone.erb'),
      owner   => $tomcat::user,
      group   => $tomcat::group,
      mode    => "u=rw",
      require => Macro::Extract["extract-tomcat-${catalina_home}"],
    }

    upstart_tomcat { $tomcat_id : }
  }
}
