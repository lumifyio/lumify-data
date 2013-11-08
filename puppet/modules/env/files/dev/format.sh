#!/bin/bash -eu

sudo rm -rf /var/lib/hadoop-hdfs/cache/*
sudo rm -rf /var/zookeeper/version-2
sudo rm -rf /data0/hdfs/name
sudo rm -rf /data0/hdfs/data

sudo -u hdfs /usr/lib/hadoop/bin/hadoop namenode -format

for service in /etc/init.d/hadoop-*
do
    sudo $service start || echo "Already started, maybe"
done

sudo -u hdfs /usr/lib/hadoop/bin/hadoop dfsadmin -safemode wait

sudo /sbin/service zookeeper-server start || echo "Already started, maybe"
sudo -u accumulo /usr/lib/accumulo/bin/accumulo init
sudo /sbin/service zookeeper-server stop

for service in /etc/init.d/hadoop-*
do
    sudo $service stop
done

sudo initctl start elasticsearch
until curl -XDELETE "http://localhost:9200/_all"
do
	echo "Cannot connect to Elasticsearch, waiting 2 seconds before trying again"
	sleep 2
done
sudo initctl stop elasticsearch
