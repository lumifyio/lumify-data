#!/bin/bash 

function hadoop {
    echo "Starting hadoop..."
    for service in /etc/init.d/hadoop-*
    do
        sudo ${service} status | grep -q "is running"
        if [ $? -eq 1 ]; then
            sudo service `basename ${service}` start
        else
            echo "${service} already running"
	fi
    done
}

function zk {
    echo "Starting zookeeper..."
    sudo service zookeeper-server status | grep -q "is running"
    if [ $? -eq 1 ]; then
        sudo service zookeeper-server start
    else
        echo "zookeeper already running"
    fi
}

function accumulo {
    echo "Starting accumulo..."
    sudo initctl start accumulo-master
    sudo initctl start accumulo-gc
    sudo initctl start accumulo-logger
    sudo initctl start accumulo-monitor
    sudo initctl start accumulo-tracer
    sudo initctl start accumulo-tserver
}

function elasticsearch {
    echo "Starting elasticsearch..."
    sudo initctl status elasticsearch | grep -q running
    if [ $? -eq 1 ]; then
        sudo initctl start elasticsearch
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
  storm)
    storm nimbus
    storm supervisor
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
