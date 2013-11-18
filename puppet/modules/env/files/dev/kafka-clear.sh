#!/bin/bash

/opt/stop.sh kafka
/opt/start.sh zk

sudo rm -rf /opt/kafka/logs/*

echo "
rmr /kafka/brokers/topics
rmr /kafka/brokers/consumers
" | sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh

/opt/start.sh kafka

/opt/kafka/bin/kafka-create-topic.sh localhost:2181/kafka text
/opt/kafka/bin/kafka-create-topic.sh localhost:2181/kafka term
/opt/kafka/bin/kafka-create-topic.sh localhost:2181/kafka artifactHighlight
/opt/kafka/bin/kafka-create-topic.sh localhost:2181/kafka processedVideo
/opt/kafka/bin/kafka-create-topic.sh localhost:2181/kafka structuredData
