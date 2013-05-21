!/bin/bash

# create /var/zookeeper as zk user
sudo su -u zk /opt/zookeeper/bin/zkServer.sh start

# fix ssh for password less login. keygenssh?
sudo su -u hadoop /opt/hadoop/bin/start-all.sh
