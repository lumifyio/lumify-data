#!/bin/bash

HOST=ci.lumify.io
BASE_DIR=/var/www/maven/snapshots/com/altamiracorp

FILE_LIST=

function _download {
  local subdir=$1
  local name_match=$2
  local local_filename=$3

  local remote_filename=$(ssh root@${HOST} find ${BASE_DIR}/${subdir} -name "${name_match}" | sort | tail -1)
  if [ "${remote_filename}" ]; then
    scp root@${HOST}:${remote_filename} ${local_filename}
    FILE_LIST="${FILE_LIST} ${local_filename}"
  else
    echo "ERROR: no artifact found for ${subdir}"
  fi
}

function _md5 {
  local file=$1

  if [ "$(uname)" = 'Linux' ]; then
    md5sum ${file} | awk '{printf "https://ci.lumify.io/fingerprint/%s/? %s\n", $1, $2}'
  else
    md5 ${file} | awk '{printf "https://ci.lumify.io/fingerprint/%s/? %s\n", $4, $2}'
  fi
}


_download lumify/lumify-enterprise-tools "*with-dependencies.jar" lumify-enterprise-tools.jar
_download lumify/lumify-storm            "*with-dependencies.jar" lumify-storm.jar
_download lumify/lumify-enterprise-storm "*with-dependencies.jar" lumify-enterprise-storm.jar
_download lumify/lumify-twitter          "*with-dependencies.jar" lumify-twitter.jar
_download lumify/lumify-facebook         "*with-dependencies.jar" lumify-facebook.jar
_download lumify/lumify-wikipedia-storm  "*with-dependencies.jar" lumify-wikipedia-storm.jar
_download lumify/lumify-wikipedia-mr     "*with-dependencies.jar" lumify-wikipedia-mr.jar
_download lumify/lumify-web-war          "*.war"                  lumify.war
_download lumify/lumify-account-web      "*.war"                  account.war
_download bigtable/bigtable-ui-war       "*.war"                  bigtable-ui.war
_download jmxui/jmx-ui-webapp            "*.war"                  jmx-ui.war
_download securegraph/securegraph-tools  "*with-dependencies.jar" securegraph-tools.jar

for file in ${FILE_LIST}; do
  _md5 ${file}
done
