#!/bin/bash -eu

ZOOKEEPER_ID=`cat /var/lib/zookeeper/myid`
ZOOKEEPER_DIR="/var/lib/zookeeper"

/vagrant/deployment/control.sh localhost stop hadoop

sudo rm -rf /var/lib/hadoop-hdfs/cache/*
sudo rm -rf /data0/hdfs/name
sudo rm -rf /data0/hdfs/data

sudo -u hdfs hdfs namenode -format

/vagrant/deployment/control.sh localhost stop zookeeper

sudo rm -rf $ZOOKEEPER_DIR
sudo mkdir -p $ZOOKEEPER_DIR
sudo chown zookeeper:zookeeper $ZOOKEEPER_DIR
sudo service zookeeper-server init --myid=${ZOOKEEPER_ID} --force

/vagrant/deployment/control.sh localhost start zookeeper
/vagrant/deployment/control.sh localhost start hadoop

sudo -u hdfs hdfs dfsadmin -safemode wait

sudo -u accumulo /opt/accumulo/bin/accumulo init --instance-name lumify --password password --clear-instance-name

sudo -u hdfs hadoop fs -mkdir -p /user/history
sudo -u hdfs hadoop fs -chmod -R 1777 /user/history
sudo -u hdfs hadoop fs -chown mapred:hadoop /user/history
sudo -u hdfs hadoop fs -mkdir -p /var/log/hadoop-yarn
sudo -u hdfs hadoop fs -chown yarn:mapred /var/log/hadoop-yarn

sudo -u hdfs hadoop fs -mkdir -p /lumify/config/opennlp
sudo -u hdfs hadoop fs -put /vagrant/lumify-public/config/opennlp/* /lumify/config/opennlp
sudo -u hdfs hadoop fs -mkdir -p /lumify/config/knownEntities
sudo -u hdfs hadoop fs -put /vagrant/lumify-public/config/knownEntities/* /lumify/config/knownEntities
sudo -u hdfs hadoop fs -mkdir -p /lumify/config/opencv
sudo -u hdfs hadoop fs -put /vagrant/lumify-public/config/opencv/* /lumify/config/opencv
sudo -u hdfs hadoop fs -mkdir -p /lumify/libcache

/vagrant/deployment/control.sh localhost start elasticsearch
until curl -XDELETE "http://$(facter ipaddress_eth0):9200/_all"; do
	echo "Cannot connect to Elasticsearch, waiting 2 seconds before trying again"
	sleep 2
done

/vagrant/deployment/control.sh localhost start
