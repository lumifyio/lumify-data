#!/bin/bash 

function hadoop {
    echo ""
    echo "Hadoop"
    for service in /etc/init.d/hadoop-*
    do
        sudo ${service} status
    done
}

function accumulo {
    echo ""
    echo "Accumulo"
    sudo initctl status accumulo-master
    sudo initctl status accumulo-gc
    sudo initctl status accumulo-monitor
    sudo initctl status accumulo-tracer
    sudo initctl status accumulo-tserver
}

function zk {
    echo ""
    echo "Zookeeper"
    sudo service zookeeper-server status
}

function elasticsearch {
    echo ""
    echo "Elastic Search"
    sudo initctl status elasticsearch
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
