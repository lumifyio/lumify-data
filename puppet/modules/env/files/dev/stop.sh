#!/bin/bash -e

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
    sudo initctl stop accumulo-tserver
    sudo initctl stop accumulo-gc
    sudo initctl stop accumulo-logger
    sudo initctl stop accumulo-monitor
    sudo initctl stop accumulo-tracer
    sudo initctl stop accumulo-master
}

function blur {
    sudo -u blur /usr/lib/apache-blur/bin/stop-all.sh
}

function oozie {
    sudo service oozie stop
}

function elasticsearch {
    sudo /usr/lib/elasticsearch/bin/service/elasticsearch stop
}

function kafka {
    sudo initctl stop kafka
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
  blur)
    blur
    ;;
  oozie)
    oozie
    ;;
  elasticsearch)
    elasticsearch
    ;;
  kafka)
    kafka
    ;;
  *)
    kafka
    elasticsearch
    blur
    oozie
    accumulo
    zk
    hadoop
    ;;
esac
