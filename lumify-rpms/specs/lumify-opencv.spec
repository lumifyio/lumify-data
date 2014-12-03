Name:           lumify-opencv
Version:        $VERSION
Release:        $RELEASE
Summary:        OpenVX 1.0 is an open, royalty-free standard for cross platform acceleration of computer vision applications and libraries.
Group:          System Environment/Libraries
License:        BSD
URL:            http://opencv.org/
Source:         $SOURCE
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)

%description

%prep
%setup -q -n %{name}

%build
export JAVA_HOME=/usr/java/default
export ANT_DIR=/opt/ant
export QA_RPATHS=$[ 0x0001|0x0002|0x0010 ]
/usr/bin/cmake -DBUILD_PERF_TESTS=OFF -DBUILD_TESTS=OFF .
make %{?_smp_mflags}

%install
rm -rf %{buildroot}
export JAVA_HOME=/usr/java/default
export ANT_DIR=/opt/ant
export QA_RPATHS=$[ 0x0001|0x0002|0x0010 ]
make install DESTDIR=%{buildroot}

%clean
rm -rf %{buildroot}

%files
%defattr(-,root,root,-)
%doc README
/usr/local/bin/opencv_createsamples
/usr/local/bin/opencv_haartraining
/usr/local/bin/opencv_performance
/usr/local/bin/opencv_traincascade
/usr/local/include/opencv/*
/usr/local/include/opencv2/*
/usr/local/lib/libopencv_calib3d.so
/usr/local/lib/libopencv_calib3d.so.2.4
/usr/local/lib/libopencv_calib3d.so.2.4.9
/usr/local/lib/libopencv_contrib.so
/usr/local/lib/libopencv_contrib.so.2.4
/usr/local/lib/libopencv_contrib.so.2.4.9
/usr/local/lib/libopencv_core.so
/usr/local/lib/libopencv_core.so.2.4
/usr/local/lib/libopencv_core.so.2.4.9
/usr/local/lib/libopencv_features2d.so
/usr/local/lib/libopencv_features2d.so.2.4
/usr/local/lib/libopencv_features2d.so.2.4.9
/usr/local/lib/libopencv_flann.so
/usr/local/lib/libopencv_flann.so.2.4
/usr/local/lib/libopencv_flann.so.2.4.9
/usr/local/lib/libopencv_gpu.so
/usr/local/lib/libopencv_gpu.so.2.4
/usr/local/lib/libopencv_gpu.so.2.4.9
/usr/local/lib/libopencv_highgui.so
/usr/local/lib/libopencv_highgui.so.2.4
/usr/local/lib/libopencv_highgui.so.2.4.9
/usr/local/lib/libopencv_imgproc.so
/usr/local/lib/libopencv_imgproc.so.2.4
/usr/local/lib/libopencv_imgproc.so.2.4.9
/usr/local/lib/libopencv_legacy.so
/usr/local/lib/libopencv_legacy.so.2.4
/usr/local/lib/libopencv_legacy.so.2.4.9
/usr/local/lib/libopencv_ml.so
/usr/local/lib/libopencv_ml.so.2.4
/usr/local/lib/libopencv_ml.so.2.4.9
/usr/local/lib/libopencv_nonfree.so
/usr/local/lib/libopencv_nonfree.so.2.4
/usr/local/lib/libopencv_nonfree.so.2.4.9
/usr/local/lib/libopencv_objdetect.so
/usr/local/lib/libopencv_objdetect.so.2.4
/usr/local/lib/libopencv_objdetect.so.2.4.9
/usr/local/lib/libopencv_photo.so
/usr/local/lib/libopencv_photo.so.2.4
/usr/local/lib/libopencv_photo.so.2.4.9
/usr/local/lib/libopencv_stitching.so
/usr/local/lib/libopencv_stitching.so.2.4
/usr/local/lib/libopencv_stitching.so.2.4.9
/usr/local/lib/libopencv_superres.so
/usr/local/lib/libopencv_superres.so.2.4
/usr/local/lib/libopencv_superres.so.2.4.9
/usr/local/lib/libopencv_ts.so
/usr/local/lib/libopencv_ts.so.2.4
/usr/local/lib/libopencv_ts.so.2.4.9
/usr/local/lib/libopencv_video.so
/usr/local/lib/libopencv_video.so.2.4
/usr/local/lib/libopencv_video.so.2.4.9
/usr/local/lib/libopencv_videostab.so
/usr/local/lib/libopencv_videostab.so.2.4
/usr/local/lib/libopencv_videostab.so.2.4.9
/usr/local/lib/pkgconfig/opencv.pc
/usr/local/share/OpenCV/*

%changelog

