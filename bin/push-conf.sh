#!/bin/bash -eu

sudo -u hadoop /opt/hadoop/bin/hadoop fs -ls /conf
[ $? -eq 0 ] && sudo -u hadoop /opt/hadoop/bin/hadoop fs -rmr /conf
sudo -u hadoop /opt/hadoop/bin/hadoop fs -copyFromLocal /vagrant/conf /
sudo -u hadoop /opt/hadoop/bin/hadoop fs -lsr /conf
