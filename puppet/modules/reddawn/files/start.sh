#!/bin/bash -eu

sudo -u zk ZOO_LOG_DIR=/opt/zookeeper /opt/zookeeper/bin/zkServer.sh start
sudo -u hadoop /opt/hadoop/bin/start-all.sh
