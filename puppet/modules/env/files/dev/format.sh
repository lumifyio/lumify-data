#!/bin/bash -eu

sudo rm -rf /var/lib/hadoop-hdfs/cache/*
sudo rm -rf /var/zookeeper
sudo rm -rf /data0/hdfs/name
sudo rm -rf /data0/hdfs/data

sudo mkdir -p /var/zookeeper
sudo chown zookeeper:zookeeper /var/zookeeper/

sudo -u hdfs hdfs namenode -format
sudo -u zookeeper zookeeper-server-initialize --myid=1 --force

for service in /etc/init.d/hadoop-*
do
    sudo $service start || echo "Already started, maybe"
done

sudo -u hdfs hdfs dfsadmin -safemode wait

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
