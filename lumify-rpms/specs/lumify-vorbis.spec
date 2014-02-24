Name:           lumify-vorbis
Version:        $VERSION
Release:        $RELEASE
Summary:        Ogg Vorbis is a completely open, patent-free, professional audio encoding and streaming technology with all the benefits of Open Source.
Group:          System Environment/Libraries
License:        BSD
URL:            http://www.vorbis.com/
Source:         $SOURCE
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
BuildRequires:	lumify-ogg
Requires:       lumify-ogg

%description

%prep
%setup -q -n %{name}

%build
export QA_RPATHS=$[ 0x0001|0x0010 ]
%configure --prefix='/usr/local' --with-ogg='/usr/local'
make %{?_smp_mflags}

%install
rm -rf %{buildroot}
export QA_RPATHS=$[ 0x0001|0x0010 ]
make install DESTDIR=%{buildroot}

%clean
rm -rf %{buildroot}

%files
%defattr(-,root,root,-)
%doc README
/usr/include/vorbis/codec.h
/usr/include/vorbis/vorbisenc.h
/usr/include/vorbis/vorbisfile.h
%{_libdir}/libvorbis.a
%{_libdir}/libvorbis.la
%{_libdir}/libvorbis.so
%{_libdir}/libvorbis.so.0
%{_libdir}/libvorbis.so.0.4.6
%{_libdir}/libvorbisenc.a
%{_libdir}/libvorbisenc.la
%{_libdir}/libvorbisenc.so
%{_libdir}/libvorbisenc.so.2
%{_libdir}/libvorbisenc.so.2.0.9
%{_libdir}/libvorbisfile.a
%{_libdir}/libvorbisfile.la
%{_libdir}/libvorbisfile.so
%{_libdir}/libvorbisfile.so.3
%{_libdir}/libvorbisfile.so.3.3.5
%{_libdir}/pkgconfig/vorbis.pc
%{_libdir}/pkgconfig/vorbisenc.pc
%{_libdir}/pkgconfig/vorbisfile.pc
/usr/share/aclocal/vorbis.m4
/usr/share/doc/libvorbis-1.3.3/*

%changelog

