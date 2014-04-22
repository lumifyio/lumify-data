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

function rabbitmq {
    echo "Stopping rabbitmq..."
    if sudo service rabbitmq-server status | grep -q RabbitMQ; then
        sudo service rabbitmq-server stop
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
  rabbitmq)
    rabbitmq
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
    rabbitmq
    elasticsearch
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
