class opencv($tmpdir="/usr/local/src") {
  require ffmpeg
  require buildtools
  require java
  include macro

  $opencvVersion = "2.4.5"

  $srcdir = "${tmpdir}/opencv-${opencvVersion}"

  macro::download { "opencv-download":
    url  => "http://downloads.sourceforge.net/project/opencvlibrary/opencv-unix/${$opencvVersion}/opencv-${opencvVersion}.tar.gz",
    path => "${tmpdir}/opencv-${opencvVersion}.tar.gz",
  } -> macro::extract { 'extract-opencv':
    file    => "${tmpdir}/opencv-${opencvVersion}.tar.gz",
    path    => $tmpdir,
    creates => "${tmpdir}/opencv-${opencvVersion}",
  }

  # problem with our distribution of cmake that doesn't set the JNI_FOUND property
  exec { "opencv-java-patch":
    cwd     => "$srcdir",
    command => "/bin/sed -i 's/JNI_FOUND/1/g' $srcdir/modules/java/CMakeLists.txt",
    require => Macro::Extract['extract-opencv'],
  }

  $cmake   = "/usr/bin/cmake -DBUILD_PERF_TESTS=OFF -DBUILD_TESTS=OFF ."
  $make    = "/usr/bin/make -j${processorcount}"
  $install = "/usr/bin/make install"
  $binpath = "/usr/lib64/qt-3.3/bin:/usr/local/bin:/bin:/usr/bin:/usr/local/sbin:/usr/sbin:/sbin:/home/vagrant/bin:${tmpdir}/apache-ant-1.9.2/bin"

  exec { 'opencv-cmake' :
    cwd         => $srcdir,
    command     => $cmake,
    path        => $binpath, 
    environment => ["JAVA_HOME=/usr/java/default", "ANT_DIR=${tmpdir}/apache-ant-1.9.2"], 
    require     => Exec['opencv-java-patch'],
  }

  exec { 'opencv-build' :
    cwd         => $srcdir,
    command     => "${make} && ${install}",
    creates     => "/usr/local/share/OpenCV/java/libopencv_java245.so",
    path        => $binpath,
    environment => ["JAVA_HOME=/usr/java/default", "ANT_DIR=${tmpdir}/apache-ant-1.9.2"],
    timeout     => 0,
    require     => Exec['opencv-cmake'],
  }

  file { "/etc/profile.d/opencv.sh":
    ensure   => file,
    source   => "puppet:///modules/opencv/opencv.sh",
    owner    => "root",
    group    => "root",
    force    => true,
    require  => Exec['opencv-build'],
  }
}

