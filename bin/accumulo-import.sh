#!/bin/bash

if [[ ! -d "$1" ]]; then
  echo 'Please specify local filesystem path to import tables from'
  exit 1
fi

EXPORTED_TABLE_DIR=$1


ACCUMULO_SHELL='/usr/lib/accumulo/bin/accumulo shell -u root -p password'

for dir in $(find ${EXPORTED_TABLE_DIR} -name 'exportMetadata.zip' | xargs -L1 dirname | sed -e 's|.*/||'); do
  $ACCUMULO_SHELL -e "deletetable -f ${dir}"

  echo "importing ${dir}..."

  hadoop fs -mkdir /tmp/importTables/${dir}
  hadoop fs -put ${EXPORTED_TABLE_DIR}/${dir}/* /tmp/importTables/${dir}

  $ACCUMULO_SHELL -e "importtable ${dir} /tmp/importTables/${dir}"
done

hadoop fs -rmr /tmp/importTables
