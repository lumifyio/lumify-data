#!/bin/bash

/opt/lumify/stop.sh
service jetty stop

rm -f /var/log/accumulo/*
rm -f /var/log/elasticsearch/*
find /var/log/hadoop-* -type f -exec rm {} \;
rm -f /var/log/hue/*
rm -f /var/log/zookeeper/*
rm -f /opt/jetty/logs/*
rm -f /opt/kafka/logs/*
rm -f /opt/storm/logs/*

find /home /root -name '.bash_history' -exec rm -f {} \;
