#!/bin/bash 

function hadoop {
    echo "Starting hadoop..."
    for service in /etc/init.d/hadoop-0.20-*
    do
        sudo ${service} status | grep -q running
        if [ $? -eq 1 ]; then
            sudo ${service} start
        else
            echo "${service} already running"
	fi
    done
}

function zk {
    echo "Starting zookeeper..."
    sudo service hadoop-zookeeper-server status | grep -q "is running"
    if [ $? -eq 1 ]; then
        sudo service hadoop-zookeeper-server start
    else
        echo "zookeeper already running"
    fi
}

function accumulo {
    echo "Starting accumulo..."
    sudo -u accumulo /usr/lib/accumulo/bin/start-all.sh
}

function elasticsearch {
    echo "Starting elasticsearch..."
    sudo /usr/lib/elasticsearch/bin/service/elasticsearch status | grep -q "is running"
    if [ $? -eq 1 ]; then
        sudo /usr/lib/elasticsearch/bin/service/elasticsearch start
    else
        echo "elasticsearch already running"
    fi
}

function kafka {
    echo "Starting kafka..."
    sudo initctl status kafka | grep -q running
    if [ $? -eq 1 ]; then
        sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh create /kafka null
        sudo initctl start kafka
    else
        echo "kafka already running"
    fi
}

function storm {
    echo "Starting storm..."
    sudo initctl status storm-$1 | grep -q running
    if [ $? -eq 1 ]; then
        sudo initctl start storm-$1
    else
        echo "storm-$1 already running"
    fi
}

case "$1" in
  hadoop)
    hadoop
    ;;
  zk)
    zk
    ;;
  accumulo)
    accumulo
    ;;
  elasticsearch)
    elasticsearch
    ;;
  kafka)
    kafka
    ;;
  storm-nimbus)
    storm nimbus
    ;;
  storm-supervisor)
    storm supervisor
    ;;
  storm-ui)
    storm ui
    ;;
  "")
    hadoop
    zk
    accumulo
    elasticsearch
    kafka
    storm nimbus
    storm supervisor
    storm ui
    ;;
  *)
    echo "Invalid service to start $1"
    ;;
esac
