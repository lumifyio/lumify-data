#!/bin/bash -eu

ZOOKEEPER_ID=`cat /var/lib/zookeeper/myid`

sudo service zookeeper-server stop

sudo rm -rf /var/lib/zookeeper
sudo mkdir -p /var/lib/zookeeper
sudo chown zookeeper:zookeeper /var/lib/zookeeper
sudo service zookeeper-server init --myid=${ZOOKEEPER_ID} --force

sudo rm -rf /var/lib/hadoop-hdfs/cache/*
sudo rm -rf /data0/hdfs/name
sudo rm -rf /data0/hdfs/data

sudo -u hdfs hdfs namenode -format

for service in /etc/init.d/hadoop-*
do
    if sudo service `basename ${service}` status | grep -q "is not running"; then
        sudo service `basename ${service}` start
    else
        echo "`basename ${service}` already running"
    fi
done

sudo -u hdfs hdfs dfsadmin -safemode wait

sudo service zookeeper-server start
sudo -u accumulo /usr/lib/accumulo/bin/accumulo init
sudo /sbin/service zookeeper-server stop

for service in /etc/init.d/hadoop-*; do
    sudo $service stop
done

sudo initctl start elasticsearch
until curl -XDELETE "http://localhost:9200/_all"; do
	echo "Cannot connect to Elasticsearch, waiting 2 seconds before trying again"
	sleep 2
done
sudo initctl stop elasticsearch
