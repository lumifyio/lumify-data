#!/bin/bash

SSH_OPTS='-o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o LogLevel=QUIET'

HOSTS_FILE=$1


function _namenode {
  awk '/ +namenode/ {print $1}' ${HOSTS_FILE}
}
function _secondarynamenode {
  awk '/ +secondarynamenode/ {print $1}' ${HOSTS_FILE}
}
function _nodes {
  awk '/node[0-9]+/ {print $1}' ${HOSTS_FILE}
}
function _zk_servers {
  awk '/zk[0-9]+/ {print $1}' ${HOSTS_FILE}
}
function _accumulomaster {
  awk '/ +accumulomaster/ {print $1}' ${HOSTS_FILE}
}
function _kafka_servers {
  awk '/kafka[0-9]+/ {print $1}' ${HOSTS_FILE}
}
function _stormmaster {
  awk '/ +stormmaster/ {print $1}' ${HOSTS_FILE}
}

function _hadoop_start {
  if [ "${FORMAT_HDFS}" = 'true' ]; then
    local ready='no'
    while [ "${ready}" != 'yes' ]; do
      echo "ssh to $(_namenode) and as the hdfs user run: hdfs namenode -format"
      echo "then type 'yes' and press return"
      read ready
    done
  fi

  ssh ${SSH_OPTS} $(_namenode) service hadoop-hdfs-namenode start
  ssh ${SSH_OPTS} $(_secondarynamenode) service hadoop-hdfs-secondarynamenode start

  for node in $(_nodes); do
    echo ${node}
    if [ "${FORMAT_HDFS}" = 'true' ]; then
      for n in 1 2 3; do
        ssh ${SSH_OPTS} ${node} mkdir -p /data${n}/hadoop/tmp
        ssh ${SSH_OPTS} ${node} chown -R hdfs:hadoop /data${n}/hadoop
        ssh ${SSH_OPTS} ${node} mkdir -p /data${n}/hdfs/data /data${n}/hdfs/name
        ssh ${SSH_OPTS} ${node} chown -R hdfs:hadoop /data${n}/hdfs
        ssh ${SSH_OPTS} ${node} mkdir -p /data${n}/mapred/local
        ssh ${SSH_OPTS} ${node} chown -R mapred:hadoop /data${n}/mapred
      done
    fi
    ssh ${SSH_OPTS} ${node} service hadoop-hdfs-datanode start
    ssh ${SSH_OPTS} ${node} service hadoop-0.20-mapreduce-tasktracker start
  done

  ssh ${SSH_OPTS} ${_namenode} service hadoop-0.20-mapreduce-jobtracker start
}

function _hadoop_stop {
  ssh ${SSH_OPTS} ${namenode} service hadoop-0.20-mapreduce-jobtracker stop

  ssh ${SSH_OPTS} $(_namenode) service hadoop-hdfs-namenode stop
  ssh ${SSH_OPTS} $(_secondarynamenode) service hadoop-hdfs-secondarynamenode stop

  for node in $(_nodes); do
    echo ${node}
    ssh ${SSH_OPTS} ${node} service hadoop-0.20-mapreduce-tasktracker stop
    ssh ${SSH_OPTS} ${node} service hadoop-hdfs-datanode stop
  done
}

function _hadoop_status {
  ssh ${SSH_OPTS} $(_namenode) service hadoop-hdfs-namenode status
  ssh ${SSH_OPTS} $(_secondarynamenode) service hadoop-hdfs-secondarynamenode status

  for node in $(_nodes); do
    echo -n "${node}: "
    ssh ${SSH_OPTS} ${node} service hadoop-hdfs-datanode status
    echo -n "${node}: "
    ssh ${SSH_OPTS} ${node} service hadoop-0.20-mapreduce-tasktracker status
  done

  ssh ${SSH_OPTS} ${namenode} service hadoop-0.20-mapreduce-jobtracker status
}

function _zookeeper_start {
  for zk in $(_zk_servers); do
    echo ${zk}
    ssh ${SSH_OPTS} ${zk} service zookeeper-server start
  done
}

function _zookeeper_stop {
  for zk in $(_zk_servers); do
    echo ${zk}
    ssh ${SSH_OPTS} ${zk} service zookeeper-server stop
  done
}

function _zookeeper_status {
  for zk in $(_zk_servers); do
    echo -n "${zk}: "
    ssh ${SSH_OPTS} ${zk} service zookeeper-server status
  done
}

function _accumulo_start {
  if [ "${INIT_ACCUMULO}" = 'true' ]; then
    local ready='no'
    while [ "${ready}" != 'yes' ]; do
      echo "ssh to $(_accumulomaster) and as the accumulo user run: /usr/lib/accumulo/bin/accumulo init"
      echo "then type 'yes' and press return"
      read ready
    done
  fi

  ssh ${SSH_OPTS} $(_accumulomaster) su - accumulo -c '/usr/lib/accumulo/bin/start-here.sh'

  for node in $(_nodes); do
    echo ${node}
    ssh ${SSH_OPTS} ${node} su - accumulo -c '/usr/lib/accumulo/bin/start-here.sh'
  done

  if [ "${INIT_ACCUMULO}" = 'true' ]; then
    local ready='no'
    while [ "${ready}" != 'yes' ]; do
      echo "ssh to $(_accumulomaster) and as the accumulo user run: /usr/lib/accumulo/bin/accumulo shell -u root -e \"setauths -u root -s 'ontology'\""
      echo "then type 'yes' and press return"
      read ready
    done
  fi
}

function _accumulo_stop {
  ssh ${SSH_OPTS} $(_accumulomaster) su - accumulo -c '/usr/lib/accumulo/bin/stop-here.sh'

  for node in $(_nodes); do
    echo ${node}
    ssh ${SSH_OPTS} ${node} su - accumulo -c '/usr/lib/accumulo/bin/stop-here.sh'
  done
}

function _accumulo_status {
  echo "accumulo status not yet implemented!"
}

function _elasticsearch_start {
  for node in $(_nodes); do
    echo -n "${node}: "
    ssh ${SSH_OPTS} ${node} initctl start elasticsearch
  done
}

function _elasticsearch_stop {
  for node in $(_nodes); do
    echo -n "${node}: "
    ssh ${SSH_OPTS} ${node} initctl stop elasticsearch
  done
}

function _elasticsearch_status {
  for node in $(_nodes); do
    echo -n "${node}: "
    ssh ${SSH_OPTS} ${node} initctl status elasticsearch
  done
}

function _kafka_start {
  for kafka in $(_kafka_servers); do
    echo -n "${kafka}: "
    ssh ${SSH_OPTS} ${kafka} initctl start kafka
  done
}

function _kafka_stop {
  for kafka in $(_kafka_servers); do
    echo -n "${kafka}: "
    ssh ${SSH_OPTS} ${kafka} initctl stop kafka
  done
}

function _kafka_status {
  for kafka in $(_kafka_servers); do
    echo -n "${kafka}: "
    ssh ${SSH_OPTS} ${kafka} initctl status kafka
  done
}

function _storm_start {
  ssh ${SSH_OPTS} $(_stormmaster) initctl start storm-nimbus
  ssh ${SSH_OPTS} $(_stormmaster) initctl start storm-ui

  for node in $(_nodes); do
    echo -n "${node}: "
    ssh ${SSH_OPTS} ${node} initctl start storm-supervisor
  done
}

function _storm_stop {
  for node in $(_nodes); do
    echo -n "${node}: "
    ssh ${SSH_OPTS} ${node} initctl stop storm-supervisor
  done

  ssh ${SSH_OPTS} $(_stormmaster) initctl stop storm-ui
  ssh ${SSH_OPTS} $(_stormmaster) initctl stop storm-nimbus
}

function _storm_status {
  ssh ${SSH_OPTS} $(_stormmaster) initctl status storm-ui
  ssh ${SSH_OPTS} $(_stormmaster) initctl status storm-nimbus

  for node in $(_nodes); do
    echo -n "${node}: "
    ssh ${SSH_OPTS} ${node} initctl status storm-supervisor
  done
}

function _all_start {
  _hadoop_start
  _zookeeper_start
  _accumulo_start
  _elasticsearch_start
  _kafka_start
  _storm_start
}

function _all_stop {
  _storm_stop
  _kafka_stop
  _elasticsearch_stop
  _accumulo_stop
  _zookeeper_stop
  _hadoop_stop
}

function _all_status {
  _hadoop_status
  _zookeeper_status
  _accumulo_status
  _elasticsearch_status
  _kafka_status
  _storm_status
}

function _run {
  local pattern=$1
  local command_and_args="${@:2}"

  echo ${command_and_args}

  for host in $(awk "/${pattern}/ {print \$1}" ${HOSTS_FILE}); do
    echo ${host}
    echo "${command_and_args}" | ssh ${SSH_OPTS} ${host} bash -s
  done
}

function _usage {
  echo "$0 <hosts file> first|start|stop|restart|status [component name]"
  local z=$(echo "$0" | tr '[:print:]' ' ')
  echo "$z              run <pattern> <command and args>"
  echo "where the optional component name is one of the following:"
  awk '/function.*_start/ && ! /all/ {print $2}' $0 | sed -e 's/^_/    /' -e 's/_start//'
}


if [ ! -f "${HOSTS_FILE}" ]; then
  echo "ERROR: host file required!"
  _usage
  exit -1
fi

case "$2" in
  first)
    FORMAT_HDFS='true'
    INIT_ACCUMULO='true'
    _all_start
    ;;
  start)
    if [ "$3" ]; then
      _$3_start
    else
      _all_start
    fi
    ;;
  stop)
    if [ "$3" ]; then
      _$3_stop
    else
      _all_stop
    fi
    ;;
  restart)
    if [ "$3" ]; then
      _$3_stop
      _$3_start
    else
      _all_stop
      _all_start
    fi
    ;;
  status)
    if [ "$3" ]; then
      _$3_status
    else
      _all_status
    fi
    ;;
  run)
    if [ $# -lt 4 ]; then
      echo "ERROR: the 'run' action requires a pattern and the desired command and args"
      _usage
      exit -3
    else
      _run $3 "${@:4}"
    fi
    ;;
  *)
    echo "ERROR: action required!"
    _usage
    exit -2
    ;;
esac
