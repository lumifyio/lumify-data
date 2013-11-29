#!/bin/bash

ACCUMULO_SHELL='sudo -u accumulo /usr/lib/accumulo/bin/accumulo shell -u root -p password'
EXPORTED_TABLE_DIR=$1
for dir in $(find ${EXPORTED_TABLE_DIR}/* -type d | xargs -l1 basename); do
  echo ${dir}
  
  hadoop fs -mkdir /tmp/importTables/${dir}
  hadoop fs -put ${EXPORTED_TABLE_DIR}/${dir}/* /tmp/importTables/${dir}/
  
  $ACCUMULO_SHELL -e "importtable ${dir} /tmp/importTables/${dir}"
done

hadoop fs -rmr /tmp/importTables

