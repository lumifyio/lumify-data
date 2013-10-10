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
    sudo initctl start accumulo-master
    sudo initctl start accumulo-gc
    sudo initctl start accumulo-logger
    sudo initctl start accumulo-monitor
    sudo initctl start accumulo-tracer
    sudo initctl start accumulo-tserver
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
    sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh create /kafka null
    sudo initctl start kafka
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
