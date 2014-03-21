Name:           lumify-sphinxbase
Version:        $VERSION
Release:        $RELEASE
Summary:        Open Source Toolkit For Speech Recognition
Group:          System Environment/Libraries
License:        BSD
URL:            http://cmusphinx.sourceforge.net/wiki/
Source:         $SOURCE
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)

%description

%prep
%setup -q -n %{name}

%build
./autogen.sh
./configure
make

%install
export QA_RPATHS=$[ 0x0002 ]
make install DESTDIR=%{buildroot}

%clean
rm -rf %{buildroot}

%files
%defattr(-,root,root,-)
%doc README
/usr/local/bin/sphinx_cepview
/usr/local/bin/sphinx_cont_adseg
/usr/local/bin/sphinx_cont_fileseg
/usr/local/bin/sphinx_fe
/usr/local/bin/sphinx_jsgf2fsg
/usr/local/bin/sphinx_lm_convert
/usr/local/bin/sphinx_lm_eval
/usr/local/bin/sphinx_lm_sort
/usr/local/bin/sphinx_pitch
/usr/local/include/sphinxbase/ad.h
/usr/local/include/sphinxbase/agc.h
/usr/local/include/sphinxbase/bio.h
/usr/local/include/sphinxbase/bitvec.h
/usr/local/include/sphinxbase/byteorder.h
/usr/local/include/sphinxbase/case.h
/usr/local/include/sphinxbase/ckd_alloc.h
/usr/local/include/sphinxbase/clapack_lite.h
/usr/local/include/sphinxbase/cmd_ln.h
/usr/local/include/sphinxbase/cmn.h
/usr/local/include/sphinxbase/cont_ad.h
/usr/local/include/sphinxbase/err.h
/usr/local/include/sphinxbase/f2c.h
/usr/local/include/sphinxbase/fe.h
/usr/local/include/sphinxbase/feat.h
/usr/local/include/sphinxbase/filename.h
/usr/local/include/sphinxbase/fixpoint.h
/usr/local/include/sphinxbase/fsg_model.h
/usr/local/include/sphinxbase/genrand.h
/usr/local/include/sphinxbase/glist.h
/usr/local/include/sphinxbase/hash_table.h
/usr/local/include/sphinxbase/heap.h
/usr/local/include/sphinxbase/huff_code.h
/usr/local/include/sphinxbase/info.h
/usr/local/include/sphinxbase/jsgf.h
/usr/local/include/sphinxbase/listelem_alloc.h
/usr/local/include/sphinxbase/logmath.h
/usr/local/include/sphinxbase/matrix.h
/usr/local/include/sphinxbase/mmio.h
/usr/local/include/sphinxbase/mulaw.h
/usr/local/include/sphinxbase/ngram_model.h
/usr/local/include/sphinxbase/pio.h
/usr/local/include/sphinxbase/prim_type.h
/usr/local/include/sphinxbase/profile.h
/usr/local/include/sphinxbase/sbthread.h
/usr/local/include/sphinxbase/sphinx_config.h
/usr/local/include/sphinxbase/sphinxbase.pxd
/usr/local/include/sphinxbase/sphinxbase_export.h
/usr/local/include/sphinxbase/strfuncs.h
/usr/local/include/sphinxbase/unlimit.h
/usr/local/include/sphinxbase/yin.h
/usr/local/lib/libsphinxad.a
/usr/local/lib/libsphinxad.la
/usr/local/lib/libsphinxad.so
/usr/local/lib/libsphinxad.so.0
/usr/local/lib/libsphinxad.so.0.0.1
/usr/local/lib/libsphinxbase.a
/usr/local/lib/libsphinxbase.la
/usr/local/lib/libsphinxbase.so
/usr/local/lib/libsphinxbase.so.1
/usr/local/lib/libsphinxbase.so.1.1.1
/usr/local/lib/pkgconfig/sphinxbase.pc

%changelog
