Name:           lumify-tesseract
Version:        $VERSION
Release:        $RELEASE
Summary:        Tesseract is probably the most accurate open source OCR engine available.
Group:          System Environment/Libraries
License:        GPL
URL:            https://code.google.com/p/tesseract-ocr/
Source:         $SOURCE
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
BuildRequires:  lumify-leptonica
Requires:       lumify-leptonica

%description

%prep
%setup -q -n %{name}

%build
%configure --prefix='/usr/local'
export QA_RPATHS=$[ 0x0001 ]
make %{?_smp_mflags}

%install
rm -rf %{buildroot}
export QA_RPATHS=$[ 0x0001 ]
make install DESTDIR=%{buildroot}

%clean
rm -rf %{buildroot}

%files
%defattr(-,root,root,-)
%doc README
/usr/bin/ambiguous_words
/usr/bin/classifier_tester
/usr/bin/cntraining
/usr/bin/combine_tessdata
/usr/bin/dawg2wordlist
/usr/bin/mftraining
/usr/bin/shapeclustering
/usr/bin/tesseract
/usr/bin/unicharset_extractor
/usr/bin/wordlist2dawg
/usr/include/tesseract/*
%{_libdir}/libtesseract.a
%{_libdir}/libtesseract.la
%{_libdir}/libtesseract.so
%{_libdir}/libtesseract.so.3
%{_libdir}/libtesseract.so.3.0.2
%{_libdir}/pkgconfig/tesseract.pc
/usr/share/man/man1/ambiguous_words.1.gz
/usr/share/man/man1/cntraining.1.gz
/usr/share/man/man1/combine_tessdata.1.gz
/usr/share/man/man1/dawg2wordlist.1.gz
/usr/share/man/man1/mftraining.1.gz
/usr/share/man/man1/shapeclustering.1.gz
/usr/share/man/man1/tesseract.1.gz
/usr/share/man/man1/unicharset_extractor.1.gz
/usr/share/man/man1/wordlist2dawg.1.gz
/usr/share/man/man5/unicharambigs.5.gz
/usr/share/man/man5/unicharset.5.gz
/usr/share/tessdata/*

%changelog

