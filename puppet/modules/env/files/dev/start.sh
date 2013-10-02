#!/bin/bash -e

function hadoop {
    for service in /etc/init.d/hadoop-0.20-*
    do
        sudo $service restart
    done
}

function zk {
    sudo /sbin/service hadoop-zookeeper-server restart
}

function accumulo {
    sudo -u accumulo /usr/lib/accumulo/bin/start-all.sh
}

function blur {
    sudo -u blur /usr/lib/apache-blur/bin/start-all.sh
}

function oozie {
    sudo service oozie restart
}

function elasticsearch {
    sudo /usr/lib/elasticsearch/bin/service/elasticsearch start
}

function kafka {
    sudo -u kafka /opt/kafka/bin/kafka-server-start.sh /opt/kafka/config/server.properties
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
    hadoop
    zk
    accumulo
    oozie
    blur; sleep 10; sudo -u blur /usr/lib/apache-blur/bin/blur safemodewait
    elasticsearch
    kafka
    ;;
esac
