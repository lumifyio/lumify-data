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
/usr/lib64/libtheora.a
/usr/lib64/libtheora.la
/usr/lib64/libtheora.so
/usr/lib64/libtheora.so.0
/usr/lib64/libtheora.so.0.3.10
/usr/lib64/libtheoradec.a
/usr/lib64/libtheoradec.la
/usr/lib64/libtheoradec.so
/usr/lib64/libtheoradec.so.1
/usr/lib64/libtheoradec.so.1.1.4
/usr/lib64/libtheoraenc.a
/usr/lib64/libtheoraenc.la
/usr/lib64/libtheoraenc.so
/usr/lib64/libtheoraenc.so.1
/usr/lib64/libtheoraenc.so.1.1.2
/usr/lib64/pkgconfig/theora.pc
/usr/lib64/pkgconfig/theoradec.pc
/usr/lib64/pkgconfig/theoraenc.pc
/usr/share/doc/libtheora-1.1.1/*

%changelog

