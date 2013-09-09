#!/bin/bash -eu

most_recent_conf_tgz=$(ls conf-*.tgz | tail -1)
most_recent_conf=$(basename ${most_recent_conf_tgz} .tgz)
tar xzf ${most_recent_conf_tgz}
hadoop fs -rmr /conf || true
hadoop fs -put ${most_recent_conf}/conf /conf
