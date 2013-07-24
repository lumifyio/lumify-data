#!/bin/bash

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

if [ "${VIRTUALIZATION_DISABLED}" = 'true' ]; then
  cd /vagrant/oozie
  sudo -u hdfs oozie job -oozie http://localhost:11000/oozie -config workflows/job-common.properties -Doozie.wf.application.path='${nameNode}/user/${user.name}/${workflowRoot}/aggregate/all' -run
else
  vagrant ssh -c 'cd /vagrant/oozie && sudo -u hdfs oozie job -oozie http://localhost:11000/oozie -config workflows/job-common.properties -Doozie.wf.application.path=\${nameNode}/user/\${user.name}/\${workflowRoot}/aggregate/extraction -run'
fi
