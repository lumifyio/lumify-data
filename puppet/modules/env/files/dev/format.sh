#!/bin/bash -eu

ZOOKEEPER_ID=`cat /var/lib/zookeeper/myid`
ZOOKEEPER_DIR="/var/lib/zookeeper"

/opt/stop.sh hadoop

sudo rm -rf /var/lib/hadoop-hdfs/cache/*
sudo rm -rf /data0/hdfs/name
sudo rm -rf /data0/hdfs/data

sudo -u hdfs hdfs namenode -format


/opt/stop.sh zk

sudo rm -rf $ZOOKEEPER_DIR
sudo mkdir -p $ZOOKEEPER_DIR
sudo chown zookeeper:zookeeper $ZOOKEEPER_DIR
sudo service zookeeper-server init --myid=${ZOOKEEPER_ID} --force


/opt/start.sh zk
/opt/start.sh hadoop

sudo -u hdfs hdfs dfsadmin -safemode wait

sudo -u accumulo /usr/lib/accumulo/bin/accumulo init --instance-name lumify --password password --clear-instance-name

sudo -u hdfs hadoop fs -mkdir /lumify/config/opennlp
sudo -u hdfs hadoop fs -put /vagrant/conf/opennlp/* /lumify/config/opennlp
sudo -u hdfs hadoop fs -mkdir /lumify/config/knownEntities
sudo -u hdfs hadoop fs -put /vagrant/conf/knownEntities/* /lumify/config/knownEntities
sudo -u hdfs hadoop fs -mkdir /lumify/config/opencv
sudo -u hdfs hadoop fs -put /vagrant/conf/opencv/* /lumify/config/opencv

/opt/stop.sh hadoop
/opt/stop.sh zk


/opt/start.sh elasticsearch
until curl -XDELETE "http://localhost:9200/_all"; do
	echo "Cannot connect to Elasticsearch, waiting 2 seconds before trying again"
	sleep 2
done
/opt/stop.sh elasticsearch
