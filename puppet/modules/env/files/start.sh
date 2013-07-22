#!/bin/bash -eu

for service in /etc/init.d/hadoop-0.20-*
do
    sudo $service start
done

sudo /sbin/service hadoop-zookeeper-server start
sudo -u accumulo /usr/lib/accumulo/bin/start-all.sh
sudo -u blur /usr/lib/apache-blur/bin/start-all.sh

# Remove sleep command when Blur safemodewait fails more gracefully when cluster isn't started
sleep 10

sudo -u blur /usr/lib/apache-blur/bin/blur safemodewait
