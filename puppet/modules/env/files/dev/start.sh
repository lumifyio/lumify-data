#!/bin/bash -e

function hadoop {
    echo "Starting hadoop..."
    for service in /etc/init.d/hadoop*-namenode /etc/init.d/hadoop*-secondarynamenode /etc/init.d/hadoop*-datanode /etc/init.d/hadoop*-jobtracker /etc/init.d/hadoop*-tasktracker;
    do
        if sudo service `basename ${service}` status | grep -q -v "is running"; then
            sudo service `basename ${service}` start
        else
            echo "`basename ${service}` already running"
    	fi
    done
}

function zk {
    echo "Starting zookeeper..."
    if sudo service zookeeper-server status | grep -q -v "is running"; then
        sudo service zookeeper-server start
    else
        echo "zookeeper already running"
    fi
}

function hue {
    echo "Starting Hue..."
    if sudo service hue status | grep -q -v "is running"; then
        sudo service hue start
    else
        echo "Hue already running"
    fi
}

function accumulo {
    echo "Starting accumulo..."
    if sudo initctl status accumulo-master | grep -q stop; then
        sudo initctl start accumulo-master
    fi

    if sudo initctl status accumulo-gc | grep -q stop; then
        sudo initctl start accumulo-gc
    fi

    if sudo initctl status accumulo-monitor | grep -q stop; then
        sudo initctl start accumulo-monitor
    fi

    if sudo initctl status accumulo-tracer | grep -q stop; then
        sudo initctl start accumulo-tracer
    fi

    if sudo initctl status accumulo-tserver | grep -q stop; then
        sudo initctl start accumulo-tserver
    fi
}

function elasticsearch {
    echo "Starting elasticsearch..."
    if sudo initctl status elasticsearch | grep -q stop; then
        sudo initctl start elasticsearch
    else
        echo "elasticsearch already running"
    fi
}

function kafka {
    echo "Starting kafka..."
    if sudo initctl status kafka | grep -q stop; then
        sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh create /kafka null
        sudo initctl start kafka
    else
        echo "kafka already running"
    fi
}

function storm {
    echo "Starting storm..."
    if sudo initctl status storm-$1 | grep -q stop; then
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
  hue)
    hue
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
    hue
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
