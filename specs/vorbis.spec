Name:           vorbis
Version:        1.3.3
Release:        dist
Summary:	Ogg Vorbis is a completely open, patent-free, professional audio encoding and streaming technology with all the benefits of Open Source.
Group:          System Environment/Libraries
License:        BSD
URL:            http://www.vorbis.com/
Source:         http://63.141.238.205:8081/redhat/source/%{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
BuildRequires:	ogg
Requires:	ogg

%description

%prep
%setup -q -n %{name}

%build
%configure --prefix='/usr/local' --with-ogg='/usr/local'
make %{?_smp_mflags}

%install
rm -rf %{buildroot}
make install DESTDIR=%{buildroot}

%clean
rm -rf %{buildroot}

%files
%defattr(-,root,root,-)
%doc README
/usr/include/vorbis/codec.h
/usr/include/vorbis/vorbisenc.h
/usr/include/vorbis/vorbisfile.h
/usr/lib64/libvorbis.a
/usr/lib64/libvorbis.la
/usr/lib64/libvorbis.so
/usr/lib64/libvorbis.so.0
/usr/lib64/libvorbis.so.0.4.6
/usr/lib64/libvorbisenc.a
/usr/lib64/libvorbisenc.la
/usr/lib64/libvorbisenc.so
/usr/lib64/libvorbisenc.so.2
/usr/lib64/libvorbisenc.so.2.0.9
/usr/lib64/libvorbisfile.a
/usr/lib64/libvorbisfile.la
/usr/lib64/libvorbisfile.so
/usr/lib64/libvorbisfile.so.3
/usr/lib64/libvorbisfile.so.3.3.5
/usr/lib64/pkgconfig/vorbis.pc
/usr/lib64/pkgconfig/vorbisenc.pc
/usr/lib64/pkgconfig/vorbisfile.pc
/usr/share/aclocal/vorbis.m4
/usr/share/doc/libvorbis-1.3.3/*

%changelog

