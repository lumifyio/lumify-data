#!/bin/bash -eu

sudo -u blur /opt/blur/bin/stop-all.sh
sudo -u accumulo /opt/accumulo/bin/stop-all.sh
sudo -u zk ZOO_LOG_DIR=/opt/zookeeper /opt/zookeeper/bin/zkServer.sh stop
sudo -u hadoop /opt/hadoop/bin/stop-all.sh
