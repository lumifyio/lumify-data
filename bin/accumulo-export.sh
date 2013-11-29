#!/bin/bash

LOCAL_OUTPUT_DIR=$1

ACCUMULO_SHELL='sudo -u accumulo /usr/lib/accumulo/bin/accumulo shell -u root -p password'

for table in $($ACCUMULO_SHELL -e 'tables' | grep -v $(date +'%Y')); do
  echo ${table}

  if [[ ${table} == atc_* ]]; then
    hadoop fs -mkdir /tmp/exportedTables/$table

    table_cloned=${table}_cloned

    $ACCUMULO_SHELL -e "clonetable ${table} ${table_cloned}"
    $ACCUMULO_SHELL -e "offline -t ${table_cloned}"
    $ACCUMULO_SHELL -e "exporttable -t ${table_cloned} /tmp/exportedTables/${table}"

    while true; do
      echo 'sleeping 5 seconds...'
      sleep 5
      hadoop fs -ls /tmp/exportedTables/${table}/distcp.txt /tmp/exportedTables/$table/exportMetadata.zip && break
    done
	
    mkdir -p ${LOCAL_OUTPUT_DIR}/${table}

    for src in $(hadoop fs -cat /tmp/exportedTables/${table}/distcp.txt); do
      hadoop fs -get ${src} ${LOCAL_OUTPUT_DIR}/${table}
    done

    $ACCUMULO_SHELL -e "deletetable -f ${table_cloned}"
  fi
done

hadoop fs -rmr /tmp/exportedTables
