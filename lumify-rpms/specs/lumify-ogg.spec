Name:           lumify-ogg
Version:        1.3.1
Release:        dist
Summary:	Ogg Vorbis is a completely open, patent-free, professional audio encoding and streaming technology with all the benefits of Open Source.
Group:          System Environment/Libraries
License:        BSD
URL:            http://www.vorbis.com/
Source:         http://63.141.238.205:8081/redhat/source/%{name}-%{version}.tar.gz
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
/usr/lib64/libogg.a
/usr/lib64/libogg.la
/usr/lib64/libogg.so
/usr/lib64/libogg.so.0
/usr/lib64/libogg.so.0.8.1
/usr/lib64/pkgconfig/ogg.pc
/usr/local/share/doc/libogg/*
/usr/share/aclocal/ogg.m4

%changelog

