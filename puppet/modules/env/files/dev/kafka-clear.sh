#!/bin/bash -e

function create_topic() {
    local topicName=$1

    echo "Creating topic ${topicName}"
    /opt/kafka/bin/kafka-create-topic.sh --zookeeper localhost:2181/kafka --replica 1 --partition 1 --topic ${topicName} &> /tmp/create_topic.log
}

function create_topics() {
    create_topic text
    create_topic term
    create_topic artifactHighlight
    create_topic searchIndex
    create_topic processedVideo
    create_topic structuredData
    create_topic twitterStream
}

function delete_topic() {
    local topicName=$1

    echo "Deleting topic ${topicName}"
    sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh "delete /kafka/brokers/topics/${topicName}/0" &> /tmp/delete_topic.log
    sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh "delete /kafka/brokers/topics/${topicName}" &> /tmp/delete_topic.log
}

function delete_topics() {
    delete_topic text
    delete_topic term
    delete_topic artifactHighlight
    delete_topic searchIndex
    delete_topic processedVideo
    delete_topic structuredData
    delete_topic twitterStream

    sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh "delete /kafka/brokers/topics" &> /tmp/delete_topic.log
    sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh "delete /kafka/consumers/text/192.168.33.10:9092:0" &> /tmp/delete_topic.log
    sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh "delete /kafka/consumers/text" &> /tmp/delete_topic.log
    sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh "delete /kafka/consumers/artifactHighlight/192.168.33.10:9092:0" &> /tmp/delete_topic.log
    sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh "delete /kafka/consumers/artifactHighlight" &> /tmp/delete_topic.log

    echo "
rmr /kafka/brokers/topics
rmr /kafka/brokers/consumers
    " | sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh &> /tmp/delete_topic.log
}

/opt/lumify/stop.sh kafka || 1
/opt/lumify/start.sh zk

sudo rm -rf /opt/kafka/logs/*

delete_topics

/opt/lumify/start.sh kafka

sleep 1  # kafka times a little bit of time to start

create_topics

echo ""
echo "Topic List"
/opt/kafka/bin/kafka-list-topic.sh --zookeeper localhost:2181/kafka
