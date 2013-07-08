#!/bin/bash -eu

/opt/stop.sh || sudo killall java

sudo rm -rf /var/lib/hadoop-0.20/cache/hadoop
sudo rm -rf /var/zookeeper/version-2

sudo -u hadoop /opt/hadoop/bin/hadoop namenode -format
sudo -u hadoop /opt/hadoop/bin/start-all.sh
sudo -u hadoop /opt/hadoop/bin/hadoop dfsadmin -safemode wait
sudo -u zk ZOO_LOG_DIR=/opt/zookeeper /opt/zookeeper/bin/zkServer.sh start
sudo -u accumulo /opt/accumulo/bin/accumulo init
sudo -u zk ZOO_LOG_DIR=/opt/zookeeper /opt/zookeeper/bin/zkServer.sh stop
sudo -u hadoop /opt/hadoop/bin/stop-all.sh
