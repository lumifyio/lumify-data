Name:           lumify-fdk-aac
Version:        0.1.1
Release:        dist
Summary:        A standalone library of the Fraunhofer FDK AAC code from Android.
Group:          System Environment/Libraries
License:        GNU GPL
URL:            https://github.com/mstorsjo/fdk-aac
Source:         http://63.141.238.205:8081/redhat/source/%{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)

%description

%prep
%setup -q -n %{name}

%build
/usr/bin/autoreconf -fiv
%configure --prefix='/usr/local' 
make %{?_smp_mflags}

%install
rm -rf %{buildroot}
make install DESTDIR=%{buildroot}

%clean
rm -rf %{buildroot}

%files
%defattr(-,root,root,-)
%doc NOTICE ChangeLog
/usr/include/fdk-aac/FDK_audio.h
/usr/include/fdk-aac/aacdecoder_lib.h
/usr/include/fdk-aac/aacenc_lib.h
/usr/include/fdk-aac/genericStds.h
/usr/include/fdk-aac/machine_type.h
/usr/lib64/libfdk-aac.a
/usr/lib64/libfdk-aac.la
/usr/lib64/libfdk-aac.so
/usr/lib64/libfdk-aac.so.0
/usr/lib64/libfdk-aac.so.0.0.2
/usr/lib64/pkgconfig/fdk-aac.pc

%changelog

