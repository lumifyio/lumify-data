Name:           lumify-videolan-x264
Version:        $VERSION
Release:        $RELEASE
Summary:        x264 is a free software library and application for encoding video streams into the H.264/MPEG-4 AVC compression format.
Group:          System Environment/Libraries
License:        GNU GPL
URL:            http://www.videolan.org/developers/x264.html
Source:         $SOURCE
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
BuildRequires:	yasm

%description

%prep
%setup -q -n %{name}

%build
%configure --prefix='/usr/local' --bindir='/usr/local/bin' --enable-static --disable-avs --disable-lavf --disable-ffms --disable-gpac --disable-swscale
make %{?_smp_mflags}

%install
rm -rf %{buildroot}
make install DESTDIR=%{buildroot}

%clean
rm -rf %{buildroot}

%files
%defattr(-,root,root,-)
%doc AUTHORS COPYING
/usr/include/x264.h
/usr/include/x264_config.h
%{_libdir}/libx264.a
%{_libdir}/pkgconfig/x264.pc
/usr/local/bin/x264

%changelog

