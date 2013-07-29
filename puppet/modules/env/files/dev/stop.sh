#!/bin/bash -e

case "$1" in
  hadoop)
    for service in /etc/init.d/hadoop-0.20-*
    do
        sudo $service stop
    done
    ;;
  zk)
    sudo /sbin/service hadoop-zookeeper-server stop
    ;;
  accumulo)
    sudo -u accumulo /usr/lib/accumulo/bin/stop-all.sh
    ;;
  blur)
    sudo -u blur /usr/lib/apache-blur/bin/stop-all.sh
    ;;
  *)
    for service in /etc/init.d/hadoop-0.20-*
    do
        sudo $service stop
    done

    sudo /sbin/service hadoop-zookeeper-server stop
    sudo -u accumulo /usr/lib/accumulo/bin/stop-all.sh
    sudo -u blur /usr/lib/apache-blur/bin/stop-all.sh
    ;;
esac
