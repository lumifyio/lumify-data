#!/bin/bash -eu

sudo rm -rf /var/lib/hadoop-0.20/cache/*
sudo rm -rf /var/zookeeper/version-2
sudo -u hdfs /usr/lib/hadoop/bin/hadoop namenode -format

for service in /etc/init.d/hadoop-0.20-*
do
    sudo $service start || echo "Already started, maybe"
done

sudo -u hdfs /usr/lib/hadoop/bin/hadoop dfsadmin -safemode wait

sudo /sbin/service hadoop-zookeeper-server start || echo "Already started, maybe"
sudo -u accumulo /usr/lib/accumulo/bin/accumulo init
sudo /sbin/service hadoop-zookeeper-server stop

for service in /etc/init.d/hadoop-0.20-*
do
    sudo $service stop
done

sudo /usr/lib/elasticsearch/bin/service/elasticsearch start
curl -XDELETE "http://localhost:9200/_all"
sudo /usr/lib/elasticsearch/bin/service/elasticsearch stop