Name:           lumify-theora
Version:        $VERSION
Release:        $RELEASE
Summary:        Theora is a free and open video compression format from the Xiph.org Foundation.
Group:          System Environment/Libraries
License:        BSD
URL:            http://www.theora.org/
Source:         $SOURCE
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
BuildRequires:	lumify-ogg
Requires:       lumify-ogg

%description

%prep
%setup -q -n %{name}

%build
%configure --prefix='/usr/local' --with-ogg='/usr/local' --disable-examples --disable-sdltest --disable-vorbistest
make %{?_smp_mflags}

%install
rm -rf %{buildroot}
make install DESTDIR=%{buildroot}

%clean
rm -rf %{buildroot}

%files
%defattr(-,root,root,-)
%doc README
/usr/include/theora/codec.h
/usr/include/theora/theora.h
/usr/include/theora/theoradec.h
/usr/include/theora/theoraenc.h
%{_libdir}/libtheora.a
%{_libdir}/libtheora.la
%{_libdir}/libtheora.so
%{_libdir}/libtheora.so.0
%{_libdir}/libtheora.so.0.3.10
%{_libdir}/libtheoradec.a
%{_libdir}/libtheoradec.la
%{_libdir}/libtheoradec.so
%{_libdir}/libtheoradec.so.1
%{_libdir}/libtheoradec.so.1.1.4
%{_libdir}/libtheoraenc.a
%{_libdir}/libtheoraenc.la
%{_libdir}/libtheoraenc.so
%{_libdir}/libtheoraenc.so.1
%{_libdir}/libtheoraenc.so.1.1.2
%{_libdir}/pkgconfig/theora.pc
%{_libdir}/pkgconfig/theoradec.pc
%{_libdir}/pkgconfig/theoraenc.pc
/usr/share/doc/libtheora-1.1.1/*

%changelog

