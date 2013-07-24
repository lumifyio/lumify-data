#!/bin/bash -e

case "$1" in
  hadoop)
    for service in /etc/init.d/hadoop-0.20-*
    do
        sudo $service restart
    done
    ;;
  zk)
    sudo /sbin/service hadoop-zookeeper-server restart
    ;;
  accumulo)
    sudo -u accumulo /usr/lib/accumulo/bin/start-all.sh
    ;;
  blur)
    sudo -u blur /usr/lib/apache-blur/bin/start-all.sh
    ;;
  oozie)
    sudo service oozie restart
    ;;
  *)
    for service in /etc/init.d/hadoop-0.20-*
    do
        sudo $service restart
    done

    sudo /sbin/service hadoop-zookeeper-server restart
    sudo -u accumulo /usr/lib/accumulo/bin/start-all.sh
    sudo service oozie restart

    sudo -u blur /usr/lib/apache-blur/bin/start-all.sh
    # Remove sleep command when Blur safemodewait fails more gracefully when cluster isn't started
    sleep 10
    sudo -u blur /usr/lib/apache-blur/bin/blur safemodewait
    ;;
esac
