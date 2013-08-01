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

function elasticsearch {
    sudo /usr/lib/elasticsearch/bin/service/elasticsearch stop
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
  elasticsearch)
    elasticsearch
    ;;
  *)

   blur
   accumulo
   elasticsearch
   zk
   hadoop
    ;;
esac
