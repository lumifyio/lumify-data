#!/bin/bash -eu

sudo -u hdfs /usr/lib/hadoop/bin/hadoop fs -ls /conf
[ $? -eq 0 ] && sudo -u hdfs /usr/lib/hadoop/bin/hadoop fs -rmr /conf
sudo -u hdfs /usr/lib/hadoop/bin/hadoop fs -copyFromLocal /vagrant/conf /
sudo -u hdfs /usr/lib/hadoop/bin/hadoop fs -lsr /conf
