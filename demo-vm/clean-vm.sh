#!/bin/bash

sudo cp /vagrant/demo-vm/configuration.properties /opt/lumify/config/configuration.properties

/opt/lumify/stop.sh

sudo rm -f /var/log/accumulo/*
sudo rm -f /var/log/elasticsearch/*
sudo find /var/log/hadoop-* -type f -exec rm {} \;
sudo rm -f /var/log/hue/*
sudo rm -f /var/log/zookeeper/*
sudo rm -f /opt/jetty/logs/*
sudo rm -f /opt/kafka/logs/*
sudo rm -f /opt/storm/logs/*

sudo find /home /root -name '.bash_history' -exec rm -f {} \;
