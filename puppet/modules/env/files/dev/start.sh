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
  *)

    hadoop
    zk
    accumulo
    oozie
    blur
    # Remove sleep command when Blur safemodewait fails more gracefully when cluster isn't started
    sleep 10
    sudo -u blur /usr/lib/apache-blur/bin/blur safemodewait

    elasticsearch
    ;;
esac
