Name:           lumify-fdk-aac
Version:        $VERSION
Release:        $RELEASE
Summary:        A standalone library of the Fraunhofer FDK AAC code from Android.
Group:          System Environment/Libraries
License:        GNU GPL
URL:            https://github.com/mstorsjo/fdk-aac
Source:         $SOURCE
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)

%description

%prep
%setup -q -n %{name}

%build
/usr/bin/autoreconf -fiv
%configure --prefix='/usr/local' --disable-shared
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
%{_libdir}/libfdk-aac.a
%{_libdir}/libfdk-aac.la
%{_libdir}/pkgconfig/fdk-aac.pc

%changelog

