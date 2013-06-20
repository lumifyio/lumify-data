Postgress
=========

```
curl -O http://yum.postgresql.org/9.2/redhat/rhel-6-x86_64/pgdg-centos92-9.2-6.noarch.rpm
sudo rpm -ivh pgdg-centos92-9.2-6.noarch.rpm 
sudo yum install postgresql-server

service postgresql initdb
chkconfig postgresql on
service postgresql start

# install osm2pgsql
yum install geos-devel proj-devel postgresql-devel libxml2-devel bzip2-devel 
yum install gcc-c++ protobuf-c-devel autoconf automake libtool
sudo rpm -ivh http://dl.fedoraproject.org/pub/epel/6/x86_64/epel-release-6-8.noarch.rpm
sudo yum install geos geos-devel
sudo yum install postgis
sudo yum install proj proj-devel
sudo yum install lua lua-devel
sudo yum install protobuf protobuf-devel protobuf-c protobuf-c-devel

git clone https://github.com/openstreetmap/osm2pgsql.git
cd osm2pgsql/
./autogen.sh
./configure
sed -i 's/-g -O2/-O2 -march=native -fomit-frame-pointer/' Makefile
make

# TODO: move data files to the 2TB drive
```
