Name:           lumify-ffmpeg
Version:        2.0
Release:        dist
Summary:	FFmpeg is a complete, cross-platform solution to record, convert and stream audio and video.
Group:          System Environment/Libraries
License:        LGPL
URL:            http://www.ffmpeg.org/
Source:         http://63.141.238.205:8081/redhat/source/%{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
BuildRequires:	x264, fdkaac, lame, opus, ogg, vorbis, vpx, theora
Requires:	x264, fdkaac, lame, opus, ogg, vorbis, vpx, theora

%description

%prep
%setup -q -n %{name}

%build
configure --prefix='/usr/local' --extra-cflags='-I/usr/local/include' --extra-ldflags='-L/usr/local/lib' --bindir='/usr/local/bin' --extra-libs='-ldl' --enable-gpl --enable-nonfree --enable-libfdk-aac --enable-libmp3lame --enable-libopus --enable-libvorbis --enable-libvpx --enable-libx264 --enable-libtheora
make %{?_smp_mflags}

%install
rm -rf %{buildroot}
make install DESTDIR=%{buildroot}

%clean
rm -rf %{buildroot}

%files
%defattr(-,root,root,-)
%doc README

%changelog

