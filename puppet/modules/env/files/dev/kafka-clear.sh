#!/bin/bash

/opt/lumify/stop.sh kafka
/opt/lumify/start.sh zk

sudo rm -rf /opt/kafka/logs/*

sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh "delete /kafka/brokers/topics/term/0"
sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh "delete /kafka/brokers/topics/term"
sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh "delete /kafka/brokers/topics/video/0"
sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh "delete /kafka/brokers/topics/video"
sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh "delete /kafka/brokers/topics/artifactHighlight/0"
sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh "delete /kafka/brokers/topics/artifactHighlight"
sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh "delete /kafka/brokers/topics/searchIndex/0"
sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh "delete /kafka/brokers/topics/searchIndex"
sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh "delete /kafka/brokers/topics/processedVideo/0"
sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh "delete /kafka/brokers/topics/processedVideo"
sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh "delete /kafka/brokers/topics/structuredData/0"
sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh "delete /kafka/brokers/topics/structuredData"
sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh "delete /kafka/brokers/topics/twitterStream/o"
sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh "delete /kafka/brokers/topics/twitterStream"
sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh "delete /kafka/brokers/topics"
sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh "delete /kafka/consumers/text/192.168.33.10:9092:0"
sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh "delete /kafka/consumers/text"
sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh "delete /kafka/consumers/artifactHighlight/192.168.33.10:9092:0"
sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh "delete /kafka/consumers/artifactHighlight"

echo "
rmr /kafka/brokers/topics
rmr /kafka/brokers/consumers
" | sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh

/opt/lumify/start.sh kafka

/opt/kafka/bin/kafka-create-topic.sh localhost:2181/kafka text
/opt/kafka/bin/kafka-create-topic.sh localhost:2181/kafka term
/opt/kafka/bin/kafka-create-topic.sh localhost:2181/kafka artifactHighlight
/opt/kafka/bin/kafka-create-topic.sh localhost:2181/kafka searchIndex
/opt/kafka/bin/kafka-create-topic.sh localhost:2181/kafka processedVideo
/opt/kafka/bin/kafka-create-topic.sh localhost:2181/kafka structuredData
/opt/kafka/bin/kafka-create-topic.sh localhost:2181/kafka twitterStream
