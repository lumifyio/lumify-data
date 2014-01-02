#!/bin/bash

IP_ADDRESSES="10.0.3.101 10.0.3.102 10.0.3.103 10.0.3.104"
PORTS="16700 16701 16702 16703"

ips=""
for ip in ${IP_ADDRESSES}; do
  for port in ${PORTS}; do
    if [ "${ips}" = '' ]; then
      ips="${ip}:${port}"
    else
      ips="${ips},${ip}:${port}"
    fi
  done
done

java -cp lumify-enterprise-tools-1.0-SNAPSHOT-jar-with-dependencies.jar \
  com.altamiracorp.lumify.tools.JmxClient \
  -ips ${ips}
