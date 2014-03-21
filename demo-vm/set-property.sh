#!/bin/bash

key_value="$1"

CONFIG_DIR=/opt/lumify/config

key=$(echo ${key_value} | cut -d '=' -f 1)
value=$(echo ${key_value} | cut -d '=' -f 2)

match_count=$(grep -E "^${key}" ${CONFIG_DIR}/*.properties | wc -l | sed -e 's/[^0-9]//g')
if [ ${match_count} -gt 1 ]; then
  echo "ERROR: found ${match_count} values for ${key} in ${CONFIG_DIR}"
  exit 1
elif [ ${match_count} -eq 1 ]; then
  for file in ${CONFIG_DIR}/*.properties; do
    grep -E -q "^${key}" ${file}
    if [ $? -eq 0 ]; then
      echo "changing the value of ${key} to '${value}' in ${file}"
      sed -i'' -e "s/^${key}.*/${key}=${value}/" ${file}
    fi
  done
else
  file="${CONFIG_DIR}/z-$(echo ${key} | sed -e 's/[^A-Za-z0-9]/-/g').properties"
  echo "setting the value of ${key} to '${value}' in ${file}"
  echo "${key}=${value}" > ${file}
fi
