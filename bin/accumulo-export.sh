#!/bin/bash -eu

if [[ $# -eq 0 ]] ; then
  echo 'Please specify local filesystem path to export tabled to'
  exit 1
fi

LOCAL_DIR=$1


ACCUMULO_SHELL='sudo -u accumulo /usr/lib/accumulo/bin/accumulo shell -u root -p password'

for table in $($ACCUMULO_SHELL -e 'tables' | grep -v $(date +'%Y')); do
  if [[ ${table} == atc_* ]]; then
    echo "exporting ${table}..."

    hadoop fs -mkdir /tmp/exportedTables/${table}

    table_cloned=${table}_cloned

    $ACCUMULO_SHELL -e "clonetable ${table} ${table_cloned}" 2>/dev/null
    $ACCUMULO_SHELL -e "offline -t ${table_cloned}" 2>/dev/null
    $ACCUMULO_SHELL -e "exporttable -t ${table_cloned} /tmp/exportedTables/${table}" 2>/dev/null

    while true; do
      echo 'sleeping 5 seconds...'
      sleep 5
      hadoop fs -ls /tmp/exportedTables/${table}/distcp.txt \
                    /tmp/exportedTables/$table/exportMetadata.zip &>/dev/null && break
    done
	
    mkdir -p ${LOCAL_DIR}/${table}

    for src in $(hadoop fs -cat /tmp/exportedTables/${table}/distcp.txt); do
      hadoop fs -get ${src} ${LOCAL_DIR}/${table}
    done

    $ACCUMULO_SHELL -e "deletetable -f ${table_cloned}" 2>/dev/null
  fi
done

hadoop fs -rmr /tmp/exportedTables
