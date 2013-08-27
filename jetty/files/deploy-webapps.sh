#!/bin/bash

JETTY_HOME=/opt/jetty
CONTEXTS_DIR=${JETTY_HOME}/contexts
WEBAPPS_DIR=${JETTY_HOME}/webapps

mv -v *.xml ${CONTEXTS_DIR}

for war in $(ls *.war); do
  simple_war_name=$(echo ${war} | sed -e 's/-.*\.war/.war/')
  mv -v ${war} ${WEBAPPS_DIR}/${simple_war_name}
done
