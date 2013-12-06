#!/bin/bash -e

function setArgs() {
  while [ "$1" != "" ]; do
    case $1 in
      "--zookeeper")
        shift
        zookeeper=$1
        ;;
      "--partition")
        shift
        echo "'partition' not supported in v0.7.x"
        ;;
      "--replica")
        shift
        echo "'replica' not supported in v0.7.x"
        ;;
      "--topic")
        shift
        topic=$1
        ;;
    esac
    shift
  done
}

setArgs $*

if [ "${zookeeper}" == "" ]; then
    echo "zookeeper is required argument"
    exit 1
fi

if [ "${topic}" == "" ]; then
    echo "topic is required argument"
    exit 1
fi

/opt/kafka/bin/kafka-create-topic-helper.sh "${zookeeper}" "${topic}"
