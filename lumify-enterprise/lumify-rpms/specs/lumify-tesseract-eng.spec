Name:           lumify-tesseract-eng
Version:        $VERSION
Release:        $RELEASE
Summary:        Tesseract is probably the most accurate open source OCR engine available. English training files.
Group:          System Environment/Libraries
License:        GPL
URL:            https://code.google.com/p/tesseract-ocr/
Source:         $SOURCE
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
BuildRequires:  lumify-tesseract
Requires:       lumify-tesseract

%description

%prep
%setup -q -n %{name}

%build

%install
rm -rf %{buildroot}
mkdir -p %{buildroot}/usr/share/
mv tessdata %{buildroot}/usr/share/

%clean
rm -rf %{buildroot}

%files
%defattr(-,root,root,-)
/usr/share/tessdata/*

%changelog

