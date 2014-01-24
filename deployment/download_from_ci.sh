#!/bin/bash

HOST=ci.lumify.io
BASE_DIR=/var/www/maven/com/altamiracorp

function _download {
  local subdir=$1
  local name_match=$2
  local local_filename=$3

  local remote_filename=$(ssh ${HOST} find ${BASE_DIR}/${subdir} -name "${name_match}" | sort | tail -1)
  scp ${HOST}:${remote_filename} ${local_filename}
}


_download lumify/lumify-enterprise-tools "*with-dependencies.jar" lumify-enterprise-tools-1.0-SNAPSHOT-jar-with-dependencies.jar
_download lumify/lumify-storm            "*with-dependencies.jar" lumify-storm-1.0-SNAPSHOT-jar-with-dependencies.jar
_download lumify/lumify-enterprise-storm "*with-dependencies.jar" lumify-enterprise-storm-1.0-SNAPSHOT-jar-with-dependencies.jar
_download lumify/lumify-twitter          "*with-dependencies.jar" lumify-twitter-1.0-SNAPSHOT-jar-with-dependencies.jar
_download lumify/lumify-web              "*.war"                  lumify.war
_download lumify/lumify-account-web      "*.war"                  account.war
_download bigtable/bigtable-ui           "*.war"                  bigtable-ui.war
_download jmxui/jmx-ui                   "*.war"                  jmx-ui.war
