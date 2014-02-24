Name:           lumify-ogg
Version:        $VERSION
Release:        $RELEASE
Summary:        Ogg Vorbis is a completely open, patent-free, professional audio encoding and streaming technology with all the benefits of Open Source.
Group:          System Environment/Libraries
License:        BSD
URL:            http://www.vorbis.com/
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
/usr/include/ogg/config_types.h
/usr/include/ogg/ogg.h
/usr/include/ogg/os_types.h
%{_libdir}/libogg.a
%{_libdir}/libogg.la
%{_libdir}/libogg.so
%{_libdir}/libogg.so.0
%{_libdir}/libogg.so.0.8.1
%{_libdir}/pkgconfig/ogg.pc
/usr/local/share/doc/libogg/*
/usr/share/aclocal/ogg.m4

%changelog

