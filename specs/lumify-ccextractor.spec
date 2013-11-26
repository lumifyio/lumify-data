Name:           lumify-ccextractor
Version:        0.66
Release:        dist
Summary:        A tool that analyzes video files and produces independent subtitle files from the closed captions data. CCExtractor is portable, small, and very fast. It works in Linux, Windows, and OSX.
Group:          System Environment/Libraries
License:        GNU GPL
URL:            http://ccextractor.sourceforge.net/
Source:         http://63.141.238.205:8081/redhat/source/%{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)

%description

%prep
%setup -q -n %{name}

%build
cd linux
./build

%install
rm -rf %{buildroot}
mkdir -p %{buildroot}/usr/local/bin/
cp linux/ccextractor %{buildroot}/usr/local/bin/

%clean
rm -rf %{buildroot}

%files
%defattr(-,root,root,-)
%doc docs/README.TXT
/usr/local/bin/ccextractor

%changelog

