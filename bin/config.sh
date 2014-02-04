#!/bin/bash

CONFIG_DIR=/opt/lumify/config

function _cp_files {
  local files=$1

  for file in ${files}; do
    if [ -s ${file} ]; then
      cp -fv ${file} ${CONFIG_DIR}
    else
      echo "ERROR: you're missing ${file}"
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

function _twitter {
  _cp_files "
	  lumify-twitter/docs/lumify-twitter.properties
	  lumify-twitter/docs/lumify-twitter-PASSWORDS.properties
  "
}

function _facebook {
  _cp_files "
	  lumify-facebook/docs/lumify-facebook.properties
	  lumify-facebook/docs/lumify-facebook-PASSWORDS.properties
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
  _twitter
  _facebook
  _account
fi
