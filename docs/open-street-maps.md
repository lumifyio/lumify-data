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

# install node.js (this is used to host the map tile server)
wget http://nodejs.org/dist/v0.8.25/node-v0.8.25-linux-x64.tar.gz
tar xzf node-v0.8.25-linux-x64.tar.gz
sudo mv node-v0.8.25-linux-x64 /opt/node-v0.8.25-linux-x64
sudo ln -s /opt/node-v0.8.25-linux-x64 /opt/node

# install osm2pgsql and mapnik
yum install geos-devel proj-devel postgresql-devel libxml2-devel bzip2-devel 
yum install gcc-c++ protobuf-c-devel autoconf automake libtool
sudo rpm -ivh http://dl.fedoraproject.org/pub/epel/6/x86_64/epel-release-6-8.noarch.rpm
sudo yum install -y geos geos-devel
sudo yum install -y postgis
sudo yum install -y proj proj-devel proj-epsg
sudo yum install -y lua lua-devel
sudo yum install -y protobuf protobuf-devel protobuf-c protobuf-c-devel
sudo yum install -y freetype freetype-devel
sudo yum install -y libpng-devel
sudo yum install -y libtiff-devel
sudo yum install -y libjpeg-devel
sudo yum install -y libicu-devel
sudo yum install -y python-devel

wget http://sourceforge.net/projects/boost/files/boost/1.53.0/boost_1_53_0.tar.gz/download
tar xzf boost_1_53_0.tar.gz
cd boost_1_53_0
./bootstrap.sh
sudo ./bjam install

git clone https://github.com/openstreetmap/osm2pgsql.git
cd osm2pgsql/
./autogen.sh
./configure
sed -i 's/-g -O2/-O2 -march=native -fomit-frame-pointer/' Makefile
make
sudo make install

git clone https://github.com/mapnik/mapnik.git
cd mapnik
git checkout v2.2.0
./configure
make
sudo make install

git clone git://github.com/openstreetmap/mapnik-stylesheets.git
cd mapnik-stylesheets/
./get-coastlines.sh

sudo -u postgres createuser gisuser
sudo -u postgres createdb --encoding=UTF8 --owner=gisuser gis
sudo -u postgres createlang plpgsql gis

# to shell into postgresql use: psql -U gisuser gis

psql -U gisuser -d gis -f /usr/share/pgsql/contrib/postgis-64.sql
psql -U gisuser -d gis -f /usr/share/pgsql/contrib/postgis-1.5/spatial_ref_sys.sql

# test osm2pgsql
wget http://download.bbbike.org/osm/bbbike/WashingtonDC/WashingtonDC.osm.pbf
osm2pgsql --database gis --username gisuser --slim WashingtonDC.osm.pbf

cd /data/red-dawn/map-tile-server/
/opt/node/bin/npm install
/opt/node/bin/node server.js --cachedir=/data/maptiles/

# import the whole planet... this will take a long time
osm2pgsql --database gis --username gisuser --slim planet-130606.osm.pbf

```
