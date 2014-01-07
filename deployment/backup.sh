#!/bin/bash -eu

HOSTS_FILE=$1


PREFIX=$(echo $(basename ${HOSTS_FILE}) | sed -e 's/_hosts//')
DATE_TIME=$(date +'%Y%m%dT%H%M%z')

HDFS_DIRS='/accumulo /lumify'
ZK_BASEDIR='/var/lib/zookeeper'
ES_BASEDIR='/var/lib/elasticsearch/data'

HDFS_BACKUP_PREFIX="/backup/${DATE_TIME}"
HDFS_RESTORE_PREFIX="/restore"

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

function _hadoop_fs {
  local host=$1; shift
  local cmd="hadoop fs $*"

  echo ${cmd}
  ssh ${SSH_OPTS} ${host} ${cmd}
}

function _hdfs_mkdir {
  local host=$1
  local dir=$2

  _hadoop_fs ${host} -mkdir -p ${dir}
}

function _hdfs_put {
  local host=$1
  local src=$2
  local dst=$3

  _hadoop_fs ${host} -put ${src} ${dst}
}

function _hdfs_get {
  local host=$1
  local src=$2
  local dst=$3

  _hadoop_fs ${host} -get ${src} ${dst}
}

function _zookeeper_backup {
  local dst

  for zk in $(_zk_servers); do
    dst="${HDFS_BACKUP_PREFIX}/${zk}/zookeeper"

    echo -n "${zk}: "
    _hdfs_mkdir ${zk} ${dst}
    echo -n "${zk}: "
    _hdfs_put ${zk} ${ZK_BASEDIR}/* ${dst}
  done
}

function _zookeeper_restore {
  for zk in $(_zk_servers); do
    echo -n "${zk}: "
    echo _hdfs_get ${zk} ${HDFS_RESTORE_PREFIX}/${zk}/zookeeper/* ${ZK_BASEDIR}
  done
}

function _elasticsearch_backup {
  local dst

  for node in $(_nodes); do
    dst="${HDFS_BACKUP_PREFIX}/${node}/elasticsearch"

    echo -n "${node}: "
    _hdfs_mkdir ${node} ${dst}
    echo -n "${node}: "
    _hdfs_put ${node} ${ES_BASEDIR} ${dst}
  done
}

function _elasticsearch_restore {
  for node in $(_nodes); do
    echo -n "${node}: "
    echo _hdfs_get ${node} ${HDFS_RESTORE_PREFIX}/${node}/elasticsearch ${ES_BASEDIR}
  done
}

function _distcp {
  local src=$1
  local dst=$2
  local cmd="hadoop distcp ${src} ${dst}"

  echo ${cmd}
  ssh ${SSH_OPTS} $(_first_node) ${cmd}
}

function _hdfs_backup {
  _hadoop_fs $(_first_node) -du -s -h ${HDFS_BACKUP_PREFIX}
  _distcp ${HDFS_BACKUP_PREFIX} ${S3_BASEDIR}

  for dir in ${HDFS_DIRS}; do
    _hadoop_fs $(_first_node) -du -s -h ${dir}
    _distcp ${dir} ${S3_BASEDIR}/hdfs${dir}
  done
}

function _hdfs_restore {
  _distcp ${S3_ROOT}${S3_RESTORE_DIR} ${HDFS_RESTORE_PREFIX}

  for dir in ${HDFS_DIRS}; do
    echo "hdfs mv ${HDFS_RESTORE_PREFIX}/hdfs${dir} -> ${dir}"
  done
}

function _backup_list {
  _hadoop_fs $(_first_node) hadoop fs -fs ${S3_ROOT} -ls /${PREFIX} \
                            | awk "/${PREFIX}/ && ! /_distcp/ {print \$6}"
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
    _hadoop_fs $(_first_node) -rm -f -r ${HDFS_BACKUP_PREFIX}
    _zookeeper_backup
    _elasticsearch_backup
    _hdfs_backup
    ;;
  restore)
    set +u
    if [ "$2" ]; then
      set -u
      S3_RESTORE_DIR=$(_backup_matching_date $2)
    else
      set -u
      S3_RESTORE_DIR=$(_most_recent_backup)
    fi
    _hadoop_fs $(_first_node) -rm -f -r ${HDFS_RESTORE_PREFIX}
    _hdfs_restore
    _zookeeper_restore
    _elasticsearch_restore
    ;;
esac
