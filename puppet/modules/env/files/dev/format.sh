#!/bin/bash -eu

ZOOKEEPER_ID=`cat /var/lib/zookeeper/myid`
ZOOKEEPER_DIR="/var/lib/zookeeper"

/opt/lumify/stop.sh hadoop

sudo rm -rf /var/lib/hadoop-hdfs/cache/*
sudo rm -rf /data0/hdfs/name
sudo rm -rf /data0/hdfs/data

sudo -u hdfs hdfs namenode -format

/opt/lumify/stop.sh zk

sudo rm -rf $ZOOKEEPER_DIR
sudo mkdir -p $ZOOKEEPER_DIR
sudo chown zookeeper:zookeeper $ZOOKEEPER_DIR
sudo service zookeeper-server init --myid=${ZOOKEEPER_ID} --force

/opt/lumify/start.sh zk
/opt/lumify/start.sh hadoop

sudo -u hdfs hdfs dfsadmin -safemode wait

sudo -u accumulo /usr/lib/accumulo/bin/accumulo init --instance-name lumify --password password --clear-instance-name

sudo -u hdfs hadoop fs -mkdir /lumify/config/opennlp
sudo -u hdfs hadoop fs -put /vagrant/config/opennlp/* /lumify/config/opennlp
sudo -u hdfs hadoop fs -mkdir /lumify/config/knownEntities
sudo -u hdfs hadoop fs -put /vagrant/config/knownEntities/* /lumify/config/knownEntities
sudo -u hdfs hadoop fs -mkdir /lumify/config/opencv
sudo -u hdfs hadoop fs -put /vagrant/config/opencv/* /lumify/config/opencv

/opt/lumify/start.sh elasticsearch
until curl -XDELETE "http://$(facter ipaddress_eth0):9200/_all"; do
	echo "Cannot connect to Elasticsearch, waiting 2 seconds before trying again"
	sleep 2
done

/opt/lumify/kafka-clear.sh

/opt/lumify/start.sh
