Name:           lumify-opus
Version:        1.0.3
Release:        dist
Summary:	Opus is a totally open, royalty-free, highly versatile audio codec. 
Group:          System Environment/Libraries
License:        BSD
URL:            http://www.opus-codec.org/
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
/usr/include/opus/opus.h
/usr/include/opus/opus_defines.h
/usr/include/opus/opus_multistream.h
/usr/include/opus/opus_types.h
/usr/lib64/libopus.a
/usr/lib64/libopus.la
/usr/lib64/libopus.so
/usr/lib64/libopus.so.0
/usr/lib64/libopus.so.0.4.0
/usr/lib64/pkgconfig/opus.pc
/usr/share/aclocal/opus.m4

%changelog

