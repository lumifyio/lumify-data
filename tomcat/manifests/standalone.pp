class tomcat::standalone inherits tomcat {

    $tomcat_http_port = hiera("tomcat_http_port", 8080)
    $tomcat_https_port = hiera("tomcat_https_port",8443)
    $tomcat_keystore_path = hiera("tomcat_keystore_path")
    $tomcat_keystore_password = hiera("tomcat_keystore_password")
    $tomcat_truststore_path = hiera("tomcat_truststore_path")
    $tomcat_truststore_password = hiera("tomcat_truststore_password")

    file { 'server.xml':
      ensure  => file,
      path    => "${home}/conf/server.xml",
      content => template('tomcat/server.xml.standalone.erb'),
      owner   => $user,
      group   => $group,
      mode    => "u=rw",
      require => Macro::Extract['extract-tomcat'],
    } 

}
