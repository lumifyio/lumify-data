#!/bin/bash

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

mvn_output=$(cd ${DIR}/.. && mvn clean package -Dmaven.test.skip=true)
mvn_exit=$?
if [ ${mvn_exit} -ne 0 ]; then
  echo "${mvn_output}"
  exit ${mvn_exit}
fi

if [ "${VIRTUALIZATION_DISABLED}" = 'true' ]; then
  sudo -u hdfs /usr/lib/hadoop/bin/hadoop fs -rmr oozie-libs
  sudo -u hdfs /usr/lib/hadoop/bin/hadoop fs -rmr oozie-workflows

  sudo -u hdfs /usr/lib/hadoop/bin/hadoop fs -put ${DIR}/../oozie/target/oozie-libs oozie-libs
  sudo -u hdfs /usr/lib/hadoop/bin/hadoop fs -put ${DIR}/../oozie/workflows oozie-workflows
else
  vagrant ssh -c 'sudo -u hdfs /usr/lib/hadoop/bin/hadoop fs -rmr oozie-libs'
  vagrant ssh -c 'sudo -u hdfs /usr/lib/hadoop/bin/hadoop fs -rmr oozie-workflows'

  vagrant ssh -c 'sudo -u hdfs /usr/lib/hadoop/bin/hadoop fs -put /vagrant/oozie/target/oozie-libs oozie-libs'
  vagrant ssh -c 'sudo -u hdfs /usr/lib/hadoop/bin/hadoop fs -put /vagrant/oozie/workflows oozie-workflows'
fi
