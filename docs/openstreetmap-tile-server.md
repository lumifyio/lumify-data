The following instructions can be used to build an [OpenStreetMap](http://www.openstreetmap.org/) tile server suitable for use as an alternative to Google Maps.


download the planet
-------------------
the `.bz2` file is 30GB

    mkdir -p /home/openstreetmap
    cd /home/openstreetmap
    curl -L -O http://planet.openstreetmap.org/planet/planet-latest.osm.bz2


install PostgreSQL
------------------
the database will be at least 1TB, initialize data files and update the start script to use space in `/home`

    rpm -ivh http://yum.postgresql.org/9.2/redhat/rhel-6.4-x86_64/pgdg-centos92-9.2-6.noarch.rpm
    yum install postgresql-server

    mkdir -p /home/postgres/data
    chown -R postgres:postgres /home/postgres

    sudo -u postgres initdb -D /home/postgres/data
    sed -i'' -e 's|PGDATA=.*|PGDATA=/home/postgres/data|' /etc/init.d/postgresql
    sed -i'' -e 's|PGLOG=.*|PGLOG=/home/postgres/pgstartup.log|' /etc/init.d/postgresql
    chkconfig postgresql on
    
    # TODO: disable selinux
    
    service postgresql start


install node.js
---------------

    cd /opt
    curl -O http://nodejs.org/dist/v0.10.18/node-v0.10.18-linux-x64.tar.gz
    tar xzf node-v0.10.18-linux-x64.tar.gz
    ln -s node-v0.10.18-linux-x64 node


install osm2pgsql
-----------------

    rpm -ivh http://dl.fedoraproject.org/pub/epel/6/x86_64/epel-release-6-8.noarch.rpm

    yum install geos-devel proj-devel postgresql-devel libxml2-devel bzip2-devel \
                gcc-c++ protobuf-c-devel autoconf automake libtool \
                geos geos-devel \
                postgis \
                proj proj-devel proj-epsg \
                lua lua-devel \
                protobuf protobuf-devel protobuf-c protobuf-c-devel \
                freetype freetype-devel \
                libpng-devel \
                libtiff-devel \
                libjpeg-devel \
                libicu-devel \
                python-devel \
                git \
                make \
                unzip \
                wget

    cd ~
    curl -L http://sourceforge.net/projects/boost/files/boost/1.55.0/boost_1_55_0.tar.gz/download -o boost_1_55_0.tar.gz
    tar xzf boost_1_55_0.tar.gz
    cd boost_1_55_0
    ./bootstrap.sh
    ./bjam install

    cd ~
    git clone https://github.com/openstreetmap/osm2pgsql.git
    cd osm2pgsql
    ./autogen.sh
    ./configure
    sed -i 's/-g -O2/-O2 -march=native -fomit-frame-pointer/' Makefile
    make -j4
    make install


install mapnik
--------------

    cd ~
    git clone https://github.com/mapnik/mapnik.git
    cd mapnik
    git checkout v2.2.0
    ./configure
    make -j4
    make install

    cd ~
    git clone git://github.com/openstreetmap/mapnik-stylesheets.git
    cd mapnik-stylesheets
    sed -i "s|BUNZIP2=.*|BUNZIP2=$(which bunzip2)|" get-coastlines.sh
    ./get-coastlines.sh


configure the PostgreSQL user and db
------------------------------------

    sudo -u postgres createuser gisuser
    sudo -u postgres createdb --encoding=UTF8 --owner=gisuser gis
    sudo -u postgres createlang plpgsql gis
    psql -U gisuser -d gis -f /usr/share/pgsql/contrib/postgis-64.sql
    psql -U gisuser -d gis -f /usr/share/pgsql/contrib/postgis-1.5/spatial_ref_sys.sql


test osm2pgsql
--------------

    cd /home/openstreetmap
    curl -O http://download.bbbike.org/osm/bbbike/WashingtonDC/WashingtonDC.osm.pbf
    osm2pgsql --database gis --username gisuser --slim WashingtonDC.osm.pbf


install the tile server
-----------------------

    yum install protobuf-lite-devel

    iptables -I INPUT $(iptables -L -n --line-numbers | awk '/tcp dpt:22/ {print $1}') \
             -p tcp -m state --state NEW -m tcp --dport 9999 -j ACCEPT
    service iptables save

    useradd -m maptiles
    mkdir -p /opt/map-tile-server
    chown maptiles:maptiles /opt/map-tile-server
    sudo -u maptiles git clone https://github.com/nearinfinity/map-tile-server.git /opt/map-tile-server

    mv ~/mapnik-stylesheets ~maptiles
    chown -R maptiles:maptiles ~maptiles/mapnik-stylesheets

    su - maptiles
    cd /opt/map-tile-server
    /opt/node/bin/npm install
    mkdir -p /home/maptiles/cache

    sed -i 's|<Parameter name="user">.*</Parameter>|<Parameter name="user">gisuser</Parameter>|' inc/datasource-settings.xml.inc
    sed -i 's|<!ENTITY world_boundaries ".*">|<!ENTITY world_boundaries "/home/maptiles/mapnik-stylesheets/world_boundaries">|' inc/settings.xml.inc


run the tile server
-------------------

    su - maptiles
    cd /opt/map-tile-server
    LD_LIBRARY_PATH=/usr/local/lib /opt/node/bin/node server.js \
                                                      --cachedir=/home/maptiles/cache \
                                                      --fontsdir=/usr/local/lib/mapnik/fonts

    # http://10.0.1.201:9999/openlayers/index.html


import the planet
-----------------
this will take a _very_ long time...

    cd /home/openstreetmap
    bunzip2 planet-latest.osm.bz2
    osm2pgsql --database gis --username gisuser --slim planet-latest.osm
