#!/bin/bash -e

ZOOKEEPER=localhost:2181
REPLICA=1
PARTITION=1

function print_help {
  echo "usage: $0 [--zookeeper SERVER] [--partition NUMBER] [--replica NUMBER]"
  echo ""
  echo "Options:"
  echo "  --zookeeper SERVER   Specify the zookeeper server to connect to"
  echo "  --partition NUMBER   Specify the number of partitions to use when creating topics"
  echo "  --replica NUMBER     Specify the number of replicas to use when creating topics"
}

function set_args {
  while [ "$1" != "" ]; do
    case $1 in
      "--zookeeper")
        shift
        ZOOKEEPER=$1
        ;;
      "--partition")
        shift
        PARTITION=$1
        ;;
      "--replica")
        shift
        REPLICA=$1
        ;;
      "--help")
        print_help
        exit 1
        ;;
    esac
    shift
  done
}

set_args $*

function ensure_zookeeper_dir {
  echo 'create /kafka ""' | sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh -server ${ZOOKEEPER} &> /tmp/ensure_zookeeper_dir.log
}

function create_topic {
  local topicName=$1

  echo "Creating topic ${topicName}"
  /opt/kafka/bin/kafka-create-topic.sh --zookeeper ${ZOOKEEPER}/kafka --replica ${REPLICA} --partition ${PARTITION} --topic ${topicName} &> /tmp/create_topic.log
}

function create_topics {
  create_topic document
  create_topic userImage
  create_topic text
  create_topic term
  create_topic artifactHighlight
  create_topic userArtifactHighlight
  create_topic processedVideo
  create_topic structuredData
  create_topic twitterStream
}

function delete_topics {
  echo "
rmr /kafka/brokers/topics
rmr /kafka/consumers
  " | sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh -server ${ZOOKEEPER} &> /tmp/delete_topic.log
}

ensure_zookeeper_dir

if [ -f /opt/lumify/stop.sh ]; then
  /opt/lumify/stop.sh kafka || true
  /opt/lumify/start.sh zk
else
  initctl stop kafka || true
fi

sudo rm -rf /opt/kafka/logs/*

delete_topics

if [ -f /opt/lumify/start.sh ]; then
  /opt/lumify/start.sh kafka
else
  initctl start kafka
fi

sleep 1  # kafka times a little bit of time to start

create_topics

echo ""
echo "Topic List"
/opt/kafka/bin/kafka-list-topic.sh --zookeeper ${ZOOKEEPER}/kafka
