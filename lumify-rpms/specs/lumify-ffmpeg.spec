Name:           lumify-ffmpeg
Version:        2.0
Release:        dist
Summary:	FFmpeg is a complete, cross-platform solution to record, convert and stream audio and video.
Group:          System Environment/Libraries
License:        LGPL
URL:            http://www.ffmpeg.org/
Source:         http://63.141.238.205:8081/redhat/source/%{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
BuildRequires:	lumify-videolan-x264, lumify-fdk-aac, lumify-lame, lumify-opus, lumify-ogg, lumify-vorbis, lumify-vpx, lumify-theora
Requires:	lumify-videolan-x264, lumify-fdk-aac, lumify-lame, lumify-opus, lumify-ogg, lumify-vorbis, lumify-vpx, lumify-theora

%description

%prep
%setup -q -n %{name}

%build
PKG_CONFIG_PATH=/usr/local/lib/pkgconfig ./configure --prefix='/usr/local' --extra-cflags='-I/usr/local/include -I/usr/include' --extra-ldflags='-L/usr/local/lib -lstdc++' --bindir='/usr/local/bin' --extra-libs='-ldl' --enable-gpl --enable-nonfree --enable-libfdk-aac --enable-libmp3lame --enable-libopus --enable-libvorbis --enable-libvpx --enable-libx264 --enable-libtheora
make %{?_smp_mflags}
make %{?_smp_mflags} tools/qt-faststart

%install
rm -rf %{buildroot}
mkdir -p %{buildroot}/usr/local/bin/
cp tools/qt-faststart %{buildroot}/usr/local/bin/qt-faststart
make install DESTDIR=%{buildroot}

%clean
rm -rf %{buildroot}

%files
%defattr(-,root,root,-)
%doc README
/usr/local/bin/ffmpeg
/usr/local/bin/ffprobe
/usr/local/bin/ffserver
/usr/local/bin/qt-faststart
/usr/local/include/libavcodec/*
/usr/local/include/libavdevice/*
/usr/local/include/libavfilter/*
/usr/local/include/libavformat/*
/usr/local/include/libavutil/*
/usr/local/include/libpostproc/*
/usr/local/include/libswresample/*
/usr/local/include/libswscale/*
/usr/local/lib/libavcodec.a
/usr/local/lib/libavdevice.a
/usr/local/lib/libavfilter.a
/usr/local/lib/libavformat.a
/usr/local/lib/libavutil.a
/usr/local/lib/libpostproc.a
/usr/local/lib/libswresample.a
/usr/local/lib/libswscale.a
/usr/local/lib/pkgconfig/libavcodec.pc
/usr/local/lib/pkgconfig/libavdevice.pc
/usr/local/lib/pkgconfig/libavfilter.pc
/usr/local/lib/pkgconfig/libavformat.pc
/usr/local/lib/pkgconfig/libavutil.pc
/usr/local/lib/pkgconfig/libpostproc.pc
/usr/local/lib/pkgconfig/libswresample.pc
/usr/local/lib/pkgconfig/libswscale.pc
/usr/local/share/ffmpeg/*
/usr/local/share/man/man1/ffmpeg-all.1
/usr/local/share/man/man1/ffmpeg-bitstream-filters.1
/usr/local/share/man/man1/ffmpeg-codecs.1
/usr/local/share/man/man1/ffmpeg-devices.1
/usr/local/share/man/man1/ffmpeg-filters.1
/usr/local/share/man/man1/ffmpeg-formats.1
/usr/local/share/man/man1/ffmpeg-protocols.1
/usr/local/share/man/man1/ffmpeg-resampler.1
/usr/local/share/man/man1/ffmpeg-scaler.1
/usr/local/share/man/man1/ffmpeg-utils.1
/usr/local/share/man/man1/ffmpeg.1
/usr/local/share/man/man1/ffprobe-all.1
/usr/local/share/man/man1/ffprobe.1
/usr/local/share/man/man1/ffserver-all.1
/usr/local/share/man/man1/ffserver.1
/usr/local/share/man/man3/libavcodec.3
/usr/local/share/man/man3/libavdevice.3
/usr/local/share/man/man3/libavfilter.3
/usr/local/share/man/man3/libavformat.3
/usr/local/share/man/man3/libavutil.3
/usr/local/share/man/man3/libswresample.3
/usr/local/share/man/man3/libswscale.3

%changelog

