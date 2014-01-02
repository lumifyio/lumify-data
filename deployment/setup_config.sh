#!/bin/bash -eu

most_recent_config_tgz=$(ls config-*.tgz | tail -1)
most_recent_config=$(basename ${most_recent_config_tgz} .tgz)
tar xzf ${most_recent_config_tgz}
hadoop fs -rm -r /lumify/config || true
hadoop fs -mkdir -p /lumify/config
hadoop fs -put ${most_recent_config}/config/* /lumify/config
