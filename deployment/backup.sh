#!/bin/bash -eu

HOSTS_FILE=$1


PREFIX=$(echo $(basename ${HOSTS_FILE}) | sed -e 's/_hosts//')
DATE_TIME=$(date +'%Y%m%dT%H%M%z')

ZK_BASEDIR='/var/lib/zookeeper'
ES_BASEDIR='/var/lib/elasticsearch/data'

HDFS_PREFIX="/backup/${DATE_TIME}"

S3_ROOT="s3://${AWS_ACCESS_KEY}:${AWS_SECRET_KEY}@${S3_BUCKET}"
S3_BASEDIR="${S3_ROOT}/${PREFIX}/${DATE_TIME}"

SSH_OPTS='-o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o LogLevel=QUIET'


function _check_env {
  if [ -z "${AWS_ACCESS_KEY}" -o -z "${AWS_SECRET_KEY}" -o -z "${S3_BUCKET}" ]; then
    echo "ERROR: the following required environment variables are not set:"
    echo "  AWS_ACCESS_KEY"
    echo "  AWS_SECRET_KEY"
    echo "  S3_BUCKET"
    exit -1
  fi

  echo "${AWS_SECRET_KEY}" | grep -Eiq '/|%2f'
  if [ $? -eq 0 ]; then
    echo "ERROR: the AWS_SECRET_KEY includes a '/' that even escaped will break Hadoop"
    exit -2
  fi
}

function _nodes {
  awk '/node[0-9]+/ {print $1}' ${HOSTS_FILE}
}

function _first_node {
  echo $(_nodes) | cut -d ' ' -f 1
}

function _zk_servers {
  awk '/zk[0-9]+/ {print $1}' ${HOSTS_FILE}
}

function _zookeeper_backup {
  for zk in $(_zk_servers); do
    echo "${zk}: hdfs put ${ZK_BASEDIR} -> ${HDFS_PREFIX}/${zk}/zookeeper"
  done
}

function _zookeeper_restore {
  for zk in $(_zk_servers); do
    echo "${zk}: hdfs get /restored/${zk}/zookeeper -> ${ZK_BASEDIR}"
  done
}

function _elasticsearch_backup {
  for node in $(_nodes); do
    echo "${node}: hdfs put ${ES_BASEDIR} -> ${HDFS_PREFIX}/${node}/elasticsearch"
  done
}

function _elasticsearch_restore {
  for node in $(_nodes); do
    echo "${node}: hdfs get /restored/${node}/elasticsearch -> ${ES_BASEDIR}"
  done
}

function _hdfs_backup {
  echo "distcp ${HDFS_PREFIX} -> ${S3_BASEDIR}"
}

function _hdfs_restore {
  echo "distcp ${RESTORE_SOURCE} -> /restored"
  echo "hdfs mv /restored -> *"
}

function _backup_list {
  ssh  ${SSH_OPTS} $(_first_node) hadoop fs -fs ${S3_ROOT} -ls /${PREFIX} | awk "/${PREFIX}/ && ! /_distcp/ {print \$6}"
}

function _backup_matching_date {
  local pattern=$1
  local match

  set +e
  _backup_list | grep -q ${pattern}
  if [ $? -ne 0 ]; then
    echo "ERROR: no backup for ${PREFIX} matching: ${pattern}"
    exit -3
  else
    match=$(_backup_list | grep ${pattern} | head -1)
  fi
  set -e

  echo ${match}
}

function _most_recent_backup {
  _backup_list | sort | tail -1
}


case $(basename $0 .sh) in
  backup)
    _zookeeper_backup
    _elasticsearch_backup
    _hdfs_backup
    ;;
  restore)
    set +u
    if [ "$2" ]; then
      set -u
      RESTORE_SOURCE=$(_backup_matching_date $2)
      set -u
    else
      RESTORE_SOURCE=$(_most_recent_backup)
    fi
    _hdfs_restore
    _zookeeper_restore
    _elasticsearch_restore
    ;;
esac
