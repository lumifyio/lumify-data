#!/bin/bash

/opt/stop.sh kafka

sudo rm -rf /opt/kafka/logs/*

sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh "delete /kafka/brokers/topics/image/0"
sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh "delete /kafka/brokers/topics/image"
sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh "delete /kafka/brokers/topics/text/0"
sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh "delete /kafka/brokers/topics/text"
sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh "delete /kafka/brokers/topics/document/0"
sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh "delete /kafka/brokers/topics/document"
sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh "delete /kafka/brokers/topics/term/0"
sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh "delete /kafka/brokers/topics/term"
sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh "delete /kafka/brokers/topics/video/0"
sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh "delete /kafka/brokers/topics/video"
sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh "delete /kafka/brokers/topics/artifactHighlight/0"
sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh "delete /kafka/brokers/topics/artifactHighlight"
sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh "delete /kafka/brokers/topics/structuredData/0"
sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh "delete /kafka/brokers/topics/structuredData"
sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh "delete /kafka/brokers/topics/processedVideo/0"
sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh "delete /kafka/brokers/topics/processedVideo"
sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh "delete /kafka/brokers/topics"
sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh "delete /kafka/consumers/text/192.168.33.10:9092:0"
sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh "delete /kafka/consumers/text"
sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh "delete /kafka/consumers/artifactHighlight/192.168.33.10:9092:0"
sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh "delete /kafka/consumers/artifactHighlight"

/opt/start.sh kafka

/opt/kafka/bin/kafka-create-topic.sh localhost:2181/kafka image
/opt/kafka/bin/kafka-create-topic.sh localhost:2181/kafka text
/opt/kafka/bin/kafka-create-topic.sh localhost:2181/kafka document
/opt/kafka/bin/kafka-create-topic.sh localhost:2181/kafka term
/opt/kafka/bin/kafka-create-topic.sh localhost:2181/kafka video
/opt/kafka/bin/kafka-create-topic.sh localhost:2181/kafka artifactHighlight
/opt/kafka/bin/kafka-create-topic.sh localhost:2181/kafka structuredData
/opt/kafka/bin/kafka-create-topic.sh localhost:2181/kafka processedVideo
