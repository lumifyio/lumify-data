Name:           lumify-opus
Version:        $VERSION
Release:        $RELEASE
Summary:	Opus is a totally open, royalty-free, highly versatile audio codec. 
Group:          System Environment/Libraries
License:        BSD
URL:            http://www.opus-codec.org/
Source:         $SOURCE
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)

%description

%prep
%setup -q -n %{name}

%build
%configure --prefix='/usr/local'
make %{?_smp_mflags}

%install
rm -rf %{buildroot}
make install DESTDIR=%{buildroot}

%clean
rm -rf %{buildroot}

%files
%defattr(-,root,root,-)
%doc README
/usr/include/opus/opus.h
/usr/include/opus/opus_defines.h
/usr/include/opus/opus_multistream.h
/usr/include/opus/opus_types.h
%{_libdir}/libopus.a
%{_libdir}/libopus.la
%{_libdir}/libopus.so
%{_libdir}/libopus.so.0
%{_libdir}/libopus.so.0.4.0
%{_libdir}/pkgconfig/opus.pc
/usr/share/aclocal/opus.m4

%changelog

