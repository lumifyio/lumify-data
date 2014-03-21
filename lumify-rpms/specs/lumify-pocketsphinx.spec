Name:           lumify-pocketsphinx
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
export LD_LIBRARY_PATH=/usr/local/lib
export PKG_CONFIG_PATH=/usr/local/lib/pkgconfig
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
/usr/local/bin/pocketsphinx_batch
/usr/local/bin/pocketsphinx_continuous
/usr/local/bin/pocketsphinx_mdef_convert
/usr/local/include/pocketsphinx/cmdln_macro.h
/usr/local/include/pocketsphinx/fsg_set.h
/usr/local/include/pocketsphinx/pocketsphinx.h
/usr/local/include/pocketsphinx/pocketsphinx.pxd
/usr/local/include/pocketsphinx/pocketsphinx_export.h
/usr/local/include/pocketsphinx/ps_lattice.h
/usr/local/include/pocketsphinx/ps_mllr.h
/usr/local/lib/libpocketsphinx.a
/usr/local/lib/libpocketsphinx.la
/usr/local/lib/libpocketsphinx.so
/usr/local/lib/libpocketsphinx.so.1
/usr/local/lib/libpocketsphinx.so.1.1.0
/usr/local/lib/pkgconfig/pocketsphinx.pc
/usr/local/share/man/man1/pocketsphinx_batch.1
/usr/local/share/man/man1/pocketsphinx_continuous.1
/usr/local/share/man/man1/pocketsphinx_mdef_convert.1
/usr/local/share/pocketsphinx/model/hmm/en/tidigits/feat.params
/usr/local/share/pocketsphinx/model/hmm/en/tidigits/mdef
/usr/local/share/pocketsphinx/model/hmm/en/tidigits/means
/usr/local/share/pocketsphinx/model/hmm/en/tidigits/sendump
/usr/local/share/pocketsphinx/model/hmm/en/tidigits/transition_matrices
/usr/local/share/pocketsphinx/model/hmm/en/tidigits/variances
/usr/local/share/pocketsphinx/model/hmm/en_US/hub4wsj_sc_8k/feat.params
/usr/local/share/pocketsphinx/model/hmm/en_US/hub4wsj_sc_8k/mdef
/usr/local/share/pocketsphinx/model/hmm/en_US/hub4wsj_sc_8k/means
/usr/local/share/pocketsphinx/model/hmm/en_US/hub4wsj_sc_8k/noisedict
/usr/local/share/pocketsphinx/model/hmm/en_US/hub4wsj_sc_8k/sendump
/usr/local/share/pocketsphinx/model/hmm/en_US/hub4wsj_sc_8k/transition_matrices
/usr/local/share/pocketsphinx/model/hmm/en_US/hub4wsj_sc_8k/variances
/usr/local/share/pocketsphinx/model/hmm/zh/tdt_sc_8k/feat.params
/usr/local/share/pocketsphinx/model/hmm/zh/tdt_sc_8k/mdef
/usr/local/share/pocketsphinx/model/hmm/zh/tdt_sc_8k/means
/usr/local/share/pocketsphinx/model/hmm/zh/tdt_sc_8k/noisedict
/usr/local/share/pocketsphinx/model/hmm/zh/tdt_sc_8k/sendump
/usr/local/share/pocketsphinx/model/hmm/zh/tdt_sc_8k/transition_matrices
/usr/local/share/pocketsphinx/model/hmm/zh/tdt_sc_8k/variances
/usr/local/share/pocketsphinx/model/lm/en/tidigits.DMP
/usr/local/share/pocketsphinx/model/lm/en/tidigits.dic
/usr/local/share/pocketsphinx/model/lm/en/tidigits.fsg
/usr/local/share/pocketsphinx/model/lm/en/turtle.DMP
/usr/local/share/pocketsphinx/model/lm/en/turtle.dic
/usr/local/share/pocketsphinx/model/lm/en_US/cmu07a.dic
/usr/local/share/pocketsphinx/model/lm/en_US/hub4.5000.DMP
/usr/local/share/pocketsphinx/model/lm/en_US/hub4.5000.dic
/usr/local/share/pocketsphinx/model/lm/en_US/wsj0vp.5000.DMP
/usr/local/share/pocketsphinx/model/lm/zh_CN/gigatdt.5000.DMP
/usr/local/share/pocketsphinx/model/lm/zh_CN/mandarin_notone.dic
/usr/local/share/pocketsphinx/model/lm/zh_TW/gigatdt.5000.DMP
/usr/local/share/pocketsphinx/model/lm/zh_TW/mandarin_notone.dic

%changelog
