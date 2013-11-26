Name:           lame
Version:        3.99.5
Release:        dist
Summary:        LAME is a high quality MPEG Audio Layer III (MP3) encoder licensed under the LGPL.
Group:          System Environment/Libraries
License:        LGPL
URL:            http://lame.sourceforge.net/
Source:         http://63.141.238.205:8081/redhat/source/%{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)

%description

%prep
%setup -q -n %{name}

%build
%configure --prefix='/usr/local' --bindir='/usr/local/bin' --enable-nasm
make %{?_smp_mflags}

%install
rm -rf %{buildroot}
make install DESTDIR=%{buildroot}

%clean
rm -rf %{buildroot}

%files
%defattr(-,root,root,-)
%doc COPYING README
/usr/include/lame/lame.h
/usr/lib64/libmp3lame.a
/usr/lib64/libmp3lame.la
/usr/lib64/libmp3lame.so
/usr/lib64/libmp3lame.so.0
/usr/lib64/libmp3lame.so.0.0.0
/usr/local/bin/lame
/usr/share/doc/lame/*
/usr/share/man/man1/lame.1.gz

%changelog

