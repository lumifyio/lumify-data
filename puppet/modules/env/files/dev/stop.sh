#!/bin/bash 

function hadoop {
    for service in /etc/init.d/hadoop-*
    do
        sudo $service stop
    done
}

function zk {
    sudo /sbin/service zookeeper-server stop
}

function accumulo {
    sudo initctl stop accumulo-tserver
    sudo initctl stop accumulo-gc
    sudo initctl stop accumulo-logger
    sudo initctl stop accumulo-monitor
    sudo initctl stop accumulo-tracer
    sudo initctl stop accumulo-master
}

function elasticsearch {
    sudo initctl stop elasticsearch
}

function kafka {
    sudo initctl stop kafka
}

function storm {
    sudo initctl stop storm-$1
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
  storm)
    storm nimbus
    storm supervisor
    storm ui
    ;;
  "")
    storm ui
    storm supervisor
    storm nimbus
    kafka
    elasticsearch
    accumulo
    zk
    hadoop
    ;;
  *)
    echo "Invalid command line option $1"
    ;;
esac
