#!/bin/bash -eu

sudo -u hadoop /opt/hadoop/bin/start-all.sh
sudo -u zk ZOO_LOG_DIR=/opt/zookeeper /opt/zookeeper/bin/zkServer.sh start
sudo -u accumulo /opt/accumulo/bin/start-all.sh
