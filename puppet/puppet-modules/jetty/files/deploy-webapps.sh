#!/bin/bash

JETTY_HOME=/opt/jetty
WEBAPPS_DIR=${JETTY_HOME}/webapps

mv -v *.xml ${WEBAPPS_DIR}

for war in $(ls *.war); do
  simple_war_name=$(echo ${war} | sed -e 's/-.*\.war/.war/')
  mv -v ${war} ${WEBAPPS_DIR}/${simple_war_name}
done
