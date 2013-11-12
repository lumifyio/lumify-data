#!/bin/bash -eu

ZOOKEEPER_ID=`cat /var/lib/zookeeper/myid`

sudo service zookeeper-server stop

sudo rm -rf /var/lib/zookeeper
sudo mkdir -p /var/lib/zookeeper
sudo chown zookeeper:zookeeper /var/lib/zookeeper
sudo service zookeeper-server init --myid=${ZOOKEEPER_ID} --force

/opt/stop.sh hadoop

sudo rm -rf /var/lib/hadoop-hdfs/cache/*
sudo rm -rf /data0/hdfs/name
sudo rm -rf /data0/hdfs/data

sudo -u hdfs hdfs namenode -format

/opt/start.sh hadoop

sudo -u hdfs hdfs dfsadmin -safemode wait

sudo -u hdfs hadoop fs -rm -r /accumulo || echo "No /accumulo"
sudo -u hdfs hadoop fs -mkdir /accumulo
sudo -u hdfs hadoop fs -chown accumulo:accumulo /accumulo

/opt/start.sh zk

sudo -u accumulo /usr/lib/accumulo/bin/accumulo init --instance-name lumify --password password --clear-instance-name
sudo /sbin/service zookeeper-server stop

sudo -u hdfs hadoop fs -mkdir /lumify/config/opennlp
sudo -u hdfs hadoop fs -put /vagrant/conf/opennlp/* /lumify/config/opennlp
sudo -u hdfs hadoop fs -mkdir /lumify/config/knownEntities
sudo -u hdfs hadoop fs -put /vagrant/conf/knownEntities/* /lumify/config/knownEntities
sudo -u hdfs hadoop fs -mkdir /lumify/config/opencv
sudo -u hdfs hadoop fs -put /vagrant/conf/opencv/* /lumify/config/opencv

for service in /etc/init.d/hadoop-*; do
    sudo $service stop
done

/opt/start.sh elasticsearch
until curl -XDELETE "http://localhost:9200/_all"; do
	echo "Cannot connect to Elasticsearch, waiting 2 seconds before trying again"
	sleep 2
done
/opt/stop.sh elasticsearch
