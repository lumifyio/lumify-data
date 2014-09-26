# Build Apache HTTPd 2.4.9 on CentOS 6.5

http://configure.systems/centos-6-5-building-apache-2-4-9-from-source/

sudo yum -y install rpm-build

curl -O http://archive.apache.org/dist/httpd/httpd-2.4.9.tar.bz2
rpmbuild -tb httpd-2.4.9.tar.bz2

curl -O http://archive.apache.org/dist/apr/apr-1.5.1.tar.bz2
rpmbuild -tb apr-1.5.1.tar.bz2
sudo yum -y install doxygen

rpmbuild -tb apr-1.5.1.tar.bz2
sudo yum erase apr apr-devel
sudo rpm -ivh ~/rpmbuild/RPMS/x86_64/apr-1.5.1-1.x86_64.rpm ~/rpmbuild/RPMS/x86_64/apr-devel-1.5.1-1.x86_64.rpm

curl -O http://archive.apache.org/dist/apr/apr-util-1.5.3.tar.bz2
rpmbuild -tb apr-util-1.5.3.tar.bz2
sudo yum -y install libuuid-devel postgresql-devel mysql-devel freetds-devel unixODBC-devel nss-devel

rpmbuild -tb apr-util-1.5.3.tar.bz2
sudo rpm -ivh ~/rpmbuild/RPMS/x86_64/apr-util-1.5.3-1.x86_64.rpm ~/rpmbuild/RPMS/x86_64/apr-util-devel-1.5.3-1.x86_64.rpm

curl -O http://dl.fedoraproject.org/pub/fedora/linux/releases/18/Fedora/source/SRPMS/d/distcache-1.4.5-23.src.rpm
rpmbuild --rebuild distcache-1.4.5-23.src.rpm
sudo rpm -ivh ~/rpmbuild/RPMS/x86_64/distcache-1.4.5-23.x86_64.rpm ~/rpmbuild/RPMS/x86_64/distcache-devel-1.4.5-23.x86_64.rpm

rpmbuild -tb httpd-2.4.9.tar.bz2
sudo yum -y install pcre-devel lua-devel libxml2-devel

rpmbuild -tb httpd-2.4.9.tar.bz2
