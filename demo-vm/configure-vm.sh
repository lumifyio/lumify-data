#!/bin/bash

LUMIFY_USERNAME=lumify
LUMIFY_PASSWORD=lumify

id -u ${LUMIFY_USERNAME} > /dev/null \
    || useradd ${LUMIFY_USERNAME}

id -Gn ${LUMIFY_USERNAME} | grep -q wheel \
    || usermod -a -G wheel ${LUMIFY_USERNAME}

echo "${LUMIFY_PASSWORD}
${LUMIFY_PASSWORD}" | passwd ${LUMIFY_USERNAME} 2> /dev/null

cat <<-EOM > /etc/sudoers.d/${LUMIFY_USERNAME}
${LUMIFY_USERNAME} ALL=(ALL) NOPASSWD: ALL
EOM

cat <<-EOM | tee /etc/motd | tee /etc/issue > /etc/issue.net

Welcome to the Lumify Demonstration VM
======================================
For more information about Lumify, please visit http://lumify.io
Built on $(date +'%Y-%m-%d')

EOM

cp /vagrant/lumify-public/{LICENSE,NOTICE} /opt/lumify
