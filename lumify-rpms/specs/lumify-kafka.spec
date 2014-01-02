Name:           lumify-kafka
Version:        $VERSION
Release:        $RELEASE
Summary:        Apache Kafka is publish-subscribe messaging rethought as a distributed commit log.
Group:          System Environment/Libraries
License:        LGPL
URL:            https://kafka.apache.org/
Source:         $SOURCE
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)

%description

%prep
%setup -q -n %{name}

%build
/bin/sh sbt update
/bin/sh sbt package
/bin/sh sbt assembly-package-dependency

%install
rm -rf %{buildroot}
mkdir -p %{buildroot}/opt
cp -R %{buildroot}/../../BUILD/lumify-kafka %{buildroot}/opt/kafka-%{version}
$(cd %{buildroot}/opt/ && ln -s kafka-%{version} kafka)
cd %{buildroot}/opt

%clean
rm -rf %{buildroot}

%files
%defattr(-,root,root,-)
%doc README.md
/opt/kafka-0.8.0/*
/opt/kafka

%changelog

