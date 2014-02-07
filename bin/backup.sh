#!/bin/bash -eu

if [[ $# -eq 0 ]]; then
  echo 'Please specify local filesystem path to export table to.'
  exit 1
fi

LOCAL_DIR=$1

ACCUMULO_SHELL='/usr/lib/accumulo/bin/accumulo shell -u root -p password'

TABLES='
atc_artifactThumbnail
atc_audit
atc_dictionaryEntry
atc_termMention
atc_user
atc_videoFrame
atc_workspace
'

for table in ${TABLES}; do
  echo "exporting ${table}..."

  hadoop fs -mkdir /tmp/exportedTables/${table}

  TABLE_CLONED=${table}_cloned

  $ACCUMULO_SHELL -e "clonetable ${table} ${TABLE_CLONED}"
  $ACCUMULO_SHELL -e "offline -t ${TABLE_CLONED}"
  $ACCUMULO_SHELL -e "exporttable -t ${TABLE_CLONED} /tmp/exportedTables/${table}"

  while true; do
    echo 'sleeping 2 seconds...'
    sleep 2
    hadoop fs -ls /tmp/exportedTables/${table}/distcp.txt \
                  /tmp/exportedTables/${table}/exportMetadata.zip &>/dev/null && break
  done

  mkdir -p ${LOCAL_DIR}/${table}

  for src in $(hadoop fs -cat /tmp/exportedTables/${table}/distcp.txt); do
    hadoop fs -get ${src} ${LOCAL_DIR}/${table}
  done

  $ACCUMULO_SHELL -e "deletetable -f ${TABLE_CLONED}"
done

hadoop fs -rm -r /tmp/exportedTables
tar -cvzf exportTables.tar.gz -C ${LOCAL_DIR} .
