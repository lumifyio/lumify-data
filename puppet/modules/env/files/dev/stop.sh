#!/bin/bash -e

function hadoop {
    for service in /etc/init.d/hadoop-*
    do
        echo "Stopping `basename ${service}`..."
        if sudo service `basename ${service}` status | grep -q "is running"; then
            sudo service `basename ${service}` stop
        fi
    done
}

function zk {
    echo "Stopping zookeeper..."
    if sudo service zookeeper-server status | grep -q "is running"; then
        sudo service zookeeper-server stop
    fi
}

function hue {
    echo "Stopping Hue..."
    if sudo service hue status | grep -q "is running"; then
        sudo service hue stop
    fi
}

function accumulo {
    echo "Stopping accumulo-$1..."
    if sudo initctl status accumulo-$1 | grep -q running; then
        sudo initctl stop accumulo-$1
    fi
}

function elasticsearch {
    echo "Stopping elasticsearch..."
    if sudo initctl status elasticsearch | grep -q running; then
        sudo initctl stop elasticsearch
    fi
}

function kafka {
    echo "Stopping kafka..."
    if sudo initctl status kafka | grep -q running; then
        sudo initctl stop kafka
    fi
}

function storm {
    echo "Stopping storm-$1..."
    if sudo initctl status storm-$1 | grep -q running; then
        sudo initctl stop storm-$1
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
    accumulo tserver
    accumulo gc
    accumulo monitor
    accumulo tracer
    accumulo master
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
    storm ui
    storm supervisor
    storm nimbus
    kafka
    elasticsearch
    hue
    accumulo tserver
    accumulo gc
    accumulo monitor
    accumulo tracer
    accumulo master
    zk
    hadoop
    ;;
  *)
    echo "Invalid command line option $1"
    ;;
esac
