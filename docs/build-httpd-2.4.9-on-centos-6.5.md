# Build Apache HTTPd 2.4.9 on CentOS 6.5

http://configure.systems/centos-6-5-building-apache-2-4-9-from-source/

1. install `rpmbuild`

        sudo yum -y install rpm-build

1. download `httpd` source and try building

        curl -O http://archive.apache.org/dist/httpd/httpd-2.4.9.tar.bz2
        rpmbuild -tb httpd-2.4.9.tar.bz2

        # rpmbuild will fail and list missing dependencies

1. download `apr` source and try building

        curl -O http://archive.apache.org/dist/apr/apr-1.5.1.tar.bz2
        rpmbuild -tb apr-1.5.1.tar.bz2

        # rpmbuild will fail and list missing dependencies
        
1. install dependencied for `apr` and try again

        sudo yum -y install doxygen

        rpmbuild -tb apr-1.5.1.tar.bz2

1. install `apr`

        sudo yum erase apr apr-devel
        sudo rpm -ivh ~/rpmbuild/RPMS/x86_64/apr-1.5.1-1.x86_64.rpm \
                      ~/rpmbuild/RPMS/x86_64/apr-devel-1.5.1-1.x86_64.rpm

1. download `apr-util` and try building

         curl -O http://archive.apache.org/dist/apr/apr-util-1.5.3.tar.bz2
         rpmbuild -tb apr-util-1.5.3.tar.bz2

         # rpmbuild will fail and list missing dependencies

1. install dependencies for `apr-util` and try again

        sudo yum -y install libuuid-devel postgresql-devel mysql-devel freetds-devel unixODBC-devel nss-devel

        rpmbuild -tb apr-util-1.5.3.tar.bz2

1. install `apr-util`

        sudo rpm -ivh ~/rpmbuild/RPMS/x86_64/apr-util-1.5.3-1.x86_64.rpm \
                      ~/rpmbuild/RPMS/x86_64/apr-util-devel-1.5.3-1.x86_64.rpm

1. download, build, and install `distcache`

        curl -O http://dl.fedoraproject.org/pub/fedora/linux/releases/18/Fedora/source/SRPMS/d/distcache-1.4.5-23.src.rpm

        rpmbuild --rebuild distcache-1.4.5-23.src.rpm

        sudo rpm -ivh ~/rpmbuild/RPMS/x86_64/distcache-1.4.5-23.x86_64.rpm \
                      ~/rpmbuild/RPMS/x86_64/distcache-devel-1.4.5-23.x86_64.rpm

1. try building `httpd` again

        rpmbuild -tb httpd-2.4.9.tar.bz2

        # rpmbuild will fail and list final missing dependencies

1. install remaining dependencies for `httpd` and try again

        sudo yum -y install pcre-devel lua-devel libxml2-devel

        rpmbuild -tb httpd-2.4.9.tar.bz2
