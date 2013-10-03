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
    sudo -u accumulo /usr/lib/accumulo/bin/stop-all.sh
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
    sudo -u kafka JMX_PORT=10000 /opt/kafka/bin/kafka-server-stop.sh /opt/kafka/config/server.properties
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
  storm-nimbus)
    storm nimbus
    ;;
  storm-supervisor)
    storm supervisor
    ;;
  storm-ui)
    storm ui
    ;;
  *)
    storm ui
    storm supervisor
    storm nimbus
    kafka
    elasticsearch
    blur
    oozie
    accumulo
    zk
    hadoop
    ;;
esac
