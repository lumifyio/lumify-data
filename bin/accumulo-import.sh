#!/bin/bash

if [[ $# -eq 0 ]] ; then
    echo 'Please specify path of tables to import'
    exit 1
fi

ACCUMULO_SHELL='sudo -u accumulo /usr/lib/accumulo/bin/accumulo shell -u root -p password'
EXPORTED_TABLE_DIR=$1
for dir in $(find ${EXPORTED_TABLE_DIR}/* -type d | xargs -l1 basename); do
  echo ${dir}
  
  hadoop fs -mkdir /tmp/importTables/${dir}
  hadoop fs -put ${EXPORTED_TABLE_DIR}/${dir}/* /tmp/importTables/${dir}/
  
  $ACCUMULO_SHELL -e "importtable ${dir} /tmp/importTables/${dir}"
done

hadoop fs -rmr /tmp/importTables

