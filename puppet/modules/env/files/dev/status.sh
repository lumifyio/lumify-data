#!/bin/bash 

function hadoop {
    echo ""
    echo "Hadoop"
    for service in /etc/init.d/hadoop-0.20-*
    do
        sudo ${service} status
    done
}

function accumulo {
    echo ""
    echo "Accumulo"
    echo "Cannot get accumulo status"
}

function zk {
    echo ""
    echo "Zookeeper"
    sudo service hadoop-zookeeper-server status
}

function elasticsearch {
    echo ""
    echo "Elastic Search"
    sudo /usr/lib/elasticsearch/bin/service/elasticsearch status
}

function kafka {
    echo ""
    echo "Kafka"
    sudo initctl status kafka
}

function storm {
    echo ""
    echo "Storm $1"
    sudo initctl status storm-$1
}

case "$1" in
  hadoop)
    hadoop
    ;;
  zk)
    zk
    ;;
  accumulo)
    accumulo
    ;;
  elasticsearch)
    elasticsearch
    ;;
  kafka)
    kafka
    ;;
  storm-nimbus)
    storm nimbus
    ;;
  storm-supervisor)
    storm supervisor
    ;;
  storm-ui)
    storm ui
    ;;
  "")
    hadoop
    zk
    accumulo
    elasticsearch
    kafka
    storm nimbus
    storm supervisor
    storm ui
    ;;
  *)
    echo "Invalid service to start $1"
    ;;
esac
