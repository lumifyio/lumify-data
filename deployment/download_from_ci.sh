#!/bin/bash

HOST=ci.lumify.io
BASE_DIR=/var/www/maven/com/altamiracorp/lumify

function _download {
  local subdir=$1
  local name_match=$2
  local local_filename=$3

  local remote_filename=$(ssh ${HOST} find ${BASE_DIR}/${subdir} -name "${name_match}" | sort | tail -1)
  scp ${HOST}:${remote_filename} ${local_filename}
}


_downlaod lumify-enterprise-tools "*with-dependencies.jar" lumify-enterprise-tools-1.0-SNAPSHOT-jar-with-dependencies.jar
_download lumify-storm            "*with-dependencies.jar" lumify-storm-1.0-SNAPSHOT-jar-with-dependencies.jar
_download lumify-enterprise-storm "*with-dependencies.jar" lumify-enterprise-storm-1.0-SNAPSHOT-jar-with-dependencies.jar
_download lumify-twitter          "*with-dependencies.jar" lumify-twitter-1.0-SNAPSHOT-jar-with-dependencies.jar
_download lumify-web              "*.war"                  lumify-web-1.0-SNAPSHOT.war
