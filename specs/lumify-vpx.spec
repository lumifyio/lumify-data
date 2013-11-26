Name:           lumify-vpx
Version:        1.2.0
Release:        dist
Summary:	vpx
Group:          System Environment/Libraries
License:        BSD
URL:            http://www.webmproject.org/
Source:         http://63.141.238.205:8081/redhat/source/%{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)

%description

%prep
%setup -q -n %{name}

%build
%configure --prefix='/usr/local' --disable-examples
make %{?_smp_mflags}

%install
rm -rf %{buildroot}
make install DESTDIR=%{buildroot}

%clean
rm -rf %{buildroot}

%files
%defattr(-,root,root,-)
%doc README
/usr/local/include/vpx/vp8.h
/usr/local/include/vpx/vp8cx.h
/usr/local/include/vpx/vp8dx.h
/usr/local/include/vpx/vpx_codec.h
/usr/local/include/vpx/vpx_codec_impl_bottom.h
/usr/local/include/vpx/vpx_codec_impl_top.h
/usr/local/include/vpx/vpx_decoder.h
/usr/local/include/vpx/vpx_encoder.h
/usr/local/include/vpx/vpx_image.h
/usr/local/include/vpx/vpx_integer.h
/usr/local/lib/libvpx.a
/usr/local/lib/pkgconfig/vpx.pc

%changelog

