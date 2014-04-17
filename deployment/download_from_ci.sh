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

function _download_enterprise_uber_jar {
  local module_name=$1

  _download lumify/${module_name} "*with-dependencies.jar" ${module_name}.jar
}

function _md5 {
  local file=$1

  if [ "$(uname)" = 'Linux' ]; then
    md5sum ${file} | awk '{printf "https://ci.lumify.io/fingerprint/%s/? %s\n", $1, $2}'
  else
    md5 ${file} | awk '{printf "https://ci.lumify.io/fingerprint/%s/? %s\n", $4, $2}'
  fi
}


_download_enterprise_uber_jar lumify-ccextractor
_download_enterprise_uber_jar lumify-clavin
_download_enterprise_uber_jar lumify-email-extractor
_download_enterprise_uber_jar lumify-enterprise-storm
_download_enterprise_uber_jar lumify-enterprise-tools
_download_enterprise_uber_jar lumify-enterprise.iml
_download_enterprise_uber_jar lumify-java-code-ingest
_download_enterprise_uber_jar lumify-jetty-server-enterprise
_download_enterprise_uber_jar lumify-known-entity-extractor
_download_enterprise_uber_jar lumify-mapped-ingest
_download_enterprise_uber_jar lumify-opencv-object-detector
_download_enterprise_uber_jar lumify-opennlp-dictionary-extractor
_download_enterprise_uber_jar lumify-opennlp-dictionary-extractor-web
_download_enterprise_uber_jar lumify-opennlp-me-extractor
_download_enterprise_uber_jar lumify-phone-number-extractor
_download_enterprise_uber_jar lumify-sphinx
_download_enterprise_uber_jar lumify-subrip-parser
_download_enterprise_uber_jar lumify-subrip-transcript
_download_enterprise_uber_jar lumify-tesseract
_download_enterprise_uber_jar lumify-tika-mime-type
_download_enterprise_uber_jar lumify-tika-text-extractor
_download_enterprise_uber_jar lumify-youtube-transcript
_download_enterprise_uber_jar lumify-zipcode-extractor

_download lumify/lumify-import            "*with-dependencies.jar" lumify-import.jar
_download lumify/lumify-storm             "*with-dependencies.jar" lumify-storm.jar

_download lumify/lumify-wikipedia-mr      "*with-dependencies.jar" lumify-wikipedia-mr.jar
_download lumify/lumify-version-inspector "*with-dependencies.jar" lumify-version.jar
_download lumify/lumify-web-war           "*.war"                  lumify.war

#_download lumify/lumify-twitter           "*with-dependencies.jar" lumify-twitter.jar
#_download lumify/lumify-facebook          "*with-dependencies.jar" lumify-facebook.jar
#_download securegraph/securegraph-tools   "*with-dependencies.jar" securegraph-tools.jar
#_download lumify/lumify-account-web       "*.war"                  account.war
#_download bigtable/bigtable-ui-war        "*.war"                  bigtable-ui.war
#_download jmxui/jmx-ui-webapp             "*.war"                  jmx-ui.war

for file in ${FILE_LIST}; do
  _md5 ${file}
done
