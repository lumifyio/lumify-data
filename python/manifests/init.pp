class python(
	$version = '2.7.6',
	$install_dir = '/usr/local',
	$tmpdir = '/tmp',
) {
	include macro
	require buildtools
	
	$make = '/usr/bin/make'
	$make_multicore = "${make} -j${processorcount}"
	$install = "${make} altinstall"
	
	$version_components = split($version, '[.]')
	$major_version = $version_components[0]
	$minor_version = $version_components[1]
	if $major_version < 3 or ($major_version == 3 and $minor_version < 3) {
		$configure_args = "--prefix=${install_dir} --enable-unicode=ucs4 --enable-shared LDFLAGS=\"-Wl,-rpath ${install_dir}/lib\""
	}
	else {
		$configure_args = "--prefix=${install_dir} --enable-shared LDFLAGS=\"-Wl,-rpath ${install_dir}/lib\""
	}
	
	$python = "${install_dir}/bin/python${major_version}.${minor_version}"
	$easy_install = "${install_dir}/bin/easy_install-${major_version}.${minor_version}"
	$pip = "${install_dir}/bin/pip${major_version}.${minor_version}"
	
	package { 'bzip2-devel': ensure => installed }
	package { 'openssl-devel': ensure => installed }
	package { 'ncurses-devel': ensure => installed }
	package { 'sqlite-devel': ensure => installed }
	package { 'readline-devel': ensure => installed }
	package { 'tk-devel': ensure => installed }
	package { 'gdbm-devel': ensure => installed }
	package { 'db4-devel': ensure => installed }
	
	# Add ${install_dir}/lib to the system library path
	file { "/etc/ld.so.conf.d/python-lib-${version}.conf":
		ensure => file,
		mode => 'u=rw,go=r',
		content => template("python/python-lib.conf.erb")
	}
	
	exec { 'exec-ldconfig':
		command => '/sbin/ldconfig',
		require => [File["/etc/ld.so.conf.d/python-lib-${version}.conf"]]
	}
	
	# Download and install python
	macro::download { 'python-download':
		url => "http://python.org/ftp/python/${version}/Python-${version}.tgz",
		path => "${tmpdir}/Python-${version}.tgz",
	} -> macro::extract { 'extract-python':
		file => "${tmpdir}/Python-${version}.tgz",
		path => "${tmpdir}",
		creates => "${tmpdir}/Python-${version}"
	}
	
	exec { 'python-build':
		cwd => "${tmpdir}/Python-${version}",
		command => "${tmpdir}/Python-${version}/configure ${configure_args} && ${make_multicore} && ${install}",
		creates => "${python}",
		timeout => 0,
		require => [Macro::Extract['extract-python'], Package['zlib-devel'], Package['bzip2-devel'], Package['openssl-devel'], Package['ncurses-devel'],
					Package['sqlite-devel'], Package['readline-devel'], Package['tk-devel'], Package['gdbm-devel'], Package['db4-devel'], Package['libpcap-devel']]
	}
	
	# Download and install Setuptools and pip
	macro::download { 'download-setuptools':
		url => 'https://bitbucket.org/pypa/setuptools/raw/bootstrap/ez_setup.py',
		path => "${tmpdir}/ez_setup.py",
	}
	exec { 'install-setuptools':
		cwd => "${tmpdir}",
		command => "${python} ${tmpdir}/ez_setup.py",
		creates => "${easy_install}",
		timeout => 0,
		require => [Macro::Download['download-setuptools'], Exec['python-build']]
	}
	exec { 'install-pip':
		command => "${easy_install} pip",
		creates => "${pip}",
		timeout => 0,
		require => [Exec['install-setuptools']]
	}
}
