Postgress
=========

```
sudo ln -s /usr/bin/bunzip2 /bin/bunzip2

curl -O http://yum.postgresql.org/9.2/redhat/rhel-6-x86_64/pgdg-centos92-9.2-6.noarch.rpm
sudo rpm -ivh pgdg-centos92-9.2-6.noarch.rpm 
sudo yum install postgresql-server

sudo -u postgres initdb -D /data/postgresql
sudo sed -i'' -e 's/PGDATA=.*/PGDATA=\/data\/postgresql/' /etc/init.d/postgresql
sudo sed -i'' -e 's/PGLOG=.*/PGLOG=\/data\/postgresql\/pgstartup.log/' /etc/init.d/postgresql
sudo chkconfig postgresql on
sudo service postgresql start

# install osm2pgsql and mapnik
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
sudo make install

git clone git://github.com/openstreetmap/mapnik-stylesheets.git
cd mapnik-stylesheets/
./get-coastlines.sh

# TODO: move data files to the 2TB drive
```
