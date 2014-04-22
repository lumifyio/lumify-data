#!/bin/bash

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

CONFIG_DIR=/opt/lumify/config

function _cp_files {
  local files=$1

  for file in ${files}; do
    f=${DIR}/../${file}
    if [ -s ${f} ]; then
      cp -fv ${f} ${CONFIG_DIR}
    else
      echo "ERROR: you're missing ${f}"
      exit 1
    fi
  done
}

function _public {
  _cp_files "
	  lumify-public/docs/lumify.properties
	  lumify-public/docs/log4j.xml
  "
}

function _enterprise {
  _cp_files "
	  docs/lumify-enterprise.properties
	  docs/lumify-clavin.properties
  "
}

function _account {
  _cp_files "
	  lumify-account-web/docs/account.properties
	  lumify-account-web/docs/account-PASSWORDS.properties
  "
}


mkdir -p /opt/lumify/config

if [ "$1" ]; then
  _$1
else
  _public
  _enterprise
  _account
fi
