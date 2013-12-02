#!/bin/bash -eu

if [[ $# -eq 0 ]] ; then
  echo 'Please specify local filesystem path to export table to.'
  exit 1
fi

LOCAL_DIR=$1


ACCUMULO_SHELL='/usr/lib/accumulo/bin/accumulo shell -u root -p password'

for table in $($ACCUMULO_SHELL -e 'tables'); do
  if [[ ${table} == atc_* ]]; then
    echo "exporting ${table}..."

    hadoop fs -mkdir /tmp/exportedTables/${table}

    table_cloned=${table}_cloned

    $ACCUMULO_SHELL -e "clonetable ${table} ${table_cloned}"
    $ACCUMULO_SHELL -e "offline -t ${table_cloned}"
    $ACCUMULO_SHELL -e "exporttable -t ${table_cloned} /tmp/exportedTables/${table}"

    while true; do
      echo 'sleeping 2 seconds...'
      sleep 2
      hadoop fs -ls /tmp/exportedTables/${table}/distcp.txt \
                    /tmp/exportedTables/$table/exportMetadata.zip &>/dev/null && break
    done
	
    mkdir -p ${LOCAL_DIR}/${table}

    for src in $(hadoop fs -cat /tmp/exportedTables/${table}/distcp.txt); do
      hadoop fs -get ${src} ${LOCAL_DIR}/${table}
    done

    $ACCUMULO_SHELL -e "deletetable -f ${table_cloned}"
  fi
done

hadoop fs -rm -R /tmp/exportedTables
tar -cvzf exportedTables.tar.gz ${LOCAL_DIR}/
