#!/bin/bash -eu

most_recent_libs_tgz=$(ls oozie-libs-*.tgz | tail -1)
rm -rf oozie-libs
tar xzf ${most_recent_libs_tgz}
hadoop fs -rmr oozie-libs || true
hadoop fs -put oozie-libs oozie-libs

unlink jobs || true
ln -s /etc/oozie/jobs
most_recent_jobs_tgz=$(ls oozie-jobs-*.tgz | tail -1)
most_recent_jobs=$(basename ${most_recent_jobs_tgz} .tgz)
tar xzf ${most_recent_jobs_tgz}
cp jobs/* ${most_recent_jobs}/oozie/jobs
hadoop fs -rmr oozie-jobs || true
hadoop fs -put ${most_recent_jobs}/oozie/jobs oozie-jobs
