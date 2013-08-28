#!/bin/bash -eu

SSH_OPTS='-o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o LogLevel=QUIET'

elastic_ip=$1
hosts_file=$2

function git_archive {
  local prefix=$1
  local dir=$2
  set +u; local include_dirs=$3; set -u

  local datetime=$(date +"%Y%m%dT%H%M")
  local githash=$(cd ${dir}; git log -n 1 --format='%h')
  prefix="${prefix}-${datetime}-${githash}"
  local tar="/tmp/${prefix}.tar"
  local tgz="/tmp/${prefix}.tgz"

  # export HEAD
  (cd ${dir}; git archive HEAD ${include_dirs} --format=tar --prefix="${prefix}/" --output=${tar})

  # add metadata
  (cd ${dir}; git log -n 1 --format='%H%n%an <%ae>%n%ad%n%s' > /tmp/GIT_INFO.txt)
  if [ "$(uname)" = 'Linux' ]; then
    (cd /tmp; tar rf ${tar} --transform "s|^|${prefix}/|" GIT_INFO.txt)
  else
    (cd /tmp; tar rf ${tar} -s "|^|${prefix}/|" GIT_INFO.txt)
  fi
  rm /tmp/GIT_INFO.txt

  # compress
  gzip ${tar} && mv ${tar}.gz ${tgz}

  echo ${tgz}
}


modules_tgz=$(git_archive modules .. puppet)
puppet_modules_tgz=$(git_archive puppet-modules ../puppet/puppet-modules)
oozie_jobs_tgz=$(git_archive oozie-jobs .. oozie/jobs)

echo 'running maven...'
mvn_output="$(cd ..; mvn clean install -DskipTests)"
mvn_exit=$?
if [ ${mvn_exit} -ne 0 ]; then
  echo "${mvn_output}"
  exit ${mvn_exit}
else
  echo 'maven done.'
fi

war_files=$(find .. -name '*.war')
oozie_libs_tgz=/tmp/oozie-libs.tgz
(cd ../oozie; tar czf ${oozie_libs_tgz} oozie-libs)

scp ${SSH_OPTS} ../aws/bin-ec2/setup_disks.sh \
                init.sh \
                run_puppet.sh \
                update.sh \
                setup_ssh.sh \
                start_*.sh \
                ${hosts_file} \
                ${modules_tgz} \
                ${puppet_modules_tgz} \
                ${oozie_jobs_tgz} \
                ${oozie_libs_tgz} \
                application.xml \
                ${war_files} \
                root@${elastic_ip}:

rm ${modules_tgz}
rm ${puppet_modules_tgz}
rm ${oozie_jobs_tgz}
rm ${oozie_libs_tgz}
