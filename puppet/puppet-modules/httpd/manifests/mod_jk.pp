class httpd::mod_jk($tmpdir="/usr/local/src") {
	require buildtools
        include macro
  require devel

	$mod_jkVersion = "1.2.37"
        $project_name  = "tomcat-connectors"
        $srcdir = "${tmpdir}/${project_name}-${mod_jkVersion}-src"
        $native = "${srcdir}/native"
        $sources = "${srcdir}.tar.gz"
        $binary = "${native}/apache-2.0/mod_jk.so"

        macro::download { "mod_jk-download":
            url  => "http://archive.apache.org/dist/tomcat/${project_name}/jk/${project_name}-${mod_jkVersion}-src.tar.gz",
            path => $sources,
        } -> macro::extract { 'extract-mod_jk':
            file    => $sources,
            path    => $tmpdir,
            creates => $srcdir,
        }
        
        $configure = "/bin/sh configure --with-apxs=/usr/sbin/apxs"
        $make      = "/usr/bin/make"
        $install   = "/usr/bin/make install"

        exec { 'mod_jk-configure' :
            cwd     => $native,
            command => $configure,
            creates => "${native}/Makefile",
            require => [Package['httpd-devel'],Macro::Extract['extract-mod_jk']],
        }
        
        exec { 'mod_jk-build' :
            cwd     => $native,
            command => "${make} && ${install}",
            creates => $binary,
            require => Exec['mod_jk-configure'],
        }
        
        file { '/usr/lib64/httpd/modules/mod_jk.so' :
            ensure => file,
            source => "file:///${binary}",
            require => Exec['mod_jk-build'],
        }
        
        $mod_jk_workers = hiera_hash("mod_jk_workers")
         
        file { 'workers.properties' :
            ensure  => file,
            path    => '/etc/httpd/conf/workers.properties',
            content => template("httpd/workers.properties.erb"),
            owner   => 'root',
            group   => 'root',
            mode    => 'u=rw,go=r',
            require => File['/usr/lib64/httpd/modules/mod_jk.so'],
        }

        # in the case that specific urls are not called out in hiera, simply map all urls 
        $mod_jk_balanced_urls = hiera_array("mod_jk_balanced_urls",[ "/*" ])
        
        file { 'extra-conf' :
            ensure  => directory,
            path    => '/etc/httpd/conf/extra',
            owner   => 'root',
            group   => 'root',
            mode    => 'u=rw,go=r',
            require => File['/usr/lib64/httpd/modules/mod_jk.so'],
        }    

        file { 'uriworkermap.properties' :
            ensure  => file,
            path    => '/etc/httpd/conf/extra/uriworkermap.properties',
            content => template("httpd/uriworkermap.properties.erb"),
            owner   => 'root',
            group   => 'root',
            mode    => 'u=rw,go=r',
            require => File['extra-conf'],
        }

        file { '/etc/httpd/conf.d/mod_jk.conf' :
            ensure  => file,
            source  => "puppet:///modules/httpd/mod_jk.conf",
            owner   => 'root',
            group   => 'root',
            mode    => 'u=rw,go=r',
            require => [File['workers.properties'],File['uriworkermap.properties']],
        }
}
