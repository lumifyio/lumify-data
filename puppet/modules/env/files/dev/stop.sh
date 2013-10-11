#!/bin/bash 

function hadoop {
    for service in /etc/init.d/hadoop-0.20-*
    do
        sudo $service stop
    done
}

function zk {
    sudo /sbin/service hadoop-zookeeper-server stop
}

function accumulo {
    sudo -u accumulo /usr/lib/accumulo/bin/stop-all.sh
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
