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
sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh "delete /kafka/brokers/topics"

/opt/start.sh kafka

echo When it appears stuck, hit enter followed by Ctrl-C
read -p "Press [Enter] key to continue"
/opt/kafka/bin/kafka-console-producer.sh --zookeeper localhost:2181/kafka --topic image
/opt/kafka/bin/kafka-console-producer.sh --zookeeper localhost:2181/kafka --topic text
/opt/kafka/bin/kafka-console-producer.sh --zookeeper localhost:2181/kafka --topic document
/opt/kafka/bin/kafka-console-producer.sh --zookeeper localhost:2181/kafka --topic term
/opt/kafka/bin/kafka-console-producer.sh --zookeeper localhost:2181/kafka --topic video

